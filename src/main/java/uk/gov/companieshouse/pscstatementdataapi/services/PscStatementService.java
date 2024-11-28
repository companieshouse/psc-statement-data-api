package uk.gov.companieshouse.pscstatementdataapi.services;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.companieshouse.pscstatementdataapi.util.DateTimeUtil.isDeltaStale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dockerjava.api.exception.ConflictException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.api.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.api.exception.BadRequestException;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.model.Created;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.api.PscStatementApiService;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.transform.PscStatementTransformer;

@Service
public class PscStatementService {

  @Autowired
  private Logger logger;
  @Autowired
  PscStatementRepository pscStatementRepository;
  @Autowired
  PscStatementTransformer pscStatementTransformer;
  @Autowired
  CompanyMetricsApiService companyMetricsApiService;
  @Autowired
  CompanyExemptionsApiService companyExemptionsApiService;
  @Autowired
  PscStatementApiService apiClientService;

  public Statement retrievePscStatementFromDb(String companyNumber, String statementId) throws JsonProcessingException, ResourceNotFoundException {
    PscStatementDocument pscStatementDocument = getPscStatementDocument(companyNumber, statementId)
            .orElseThrow(() -> new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()),
                    String.format("Resource not found for statement ID: %s and company number: %s",
                            statementId, companyNumber)));
    return pscStatementDocument.getData();
  }

  public StatementList retrievePscStatementListFromDb(String companyNumber, int startIndex, boolean registerView, int itemsPerPage) {

    Optional<MetricsApi> companyMetrics = companyMetricsApiService.getCompanyMetrics(companyNumber);

    if (registerView) {
      return retrievePscStatementListFromDbRegisterView(companyNumber, companyMetrics, startIndex, itemsPerPage);
    }

    Optional<List<PscStatementDocument>> statementListOptional = pscStatementRepository.getStatementList(companyNumber, startIndex, itemsPerPage);
    List<PscStatementDocument> pscStatementDocuments = statementListOptional.filter(docs -> !docs.isEmpty()).orElseThrow(() ->
            new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()), String.format(
                    "Resource not found for company number: %s", companyNumber)));

    return createStatementList(pscStatementDocuments, startIndex, itemsPerPage, companyMetrics, companyNumber, registerView);
  }

  public StatementList retrievePscStatementListFromDbRegisterView(String companyNumber, Optional<MetricsApi> companyMetrics, int startIndex, int itemsPerPage) {

    logger.info(String.format("In register view for company number: %s", companyNumber), DataMapHolder.getLogMap());
    MetricsApi metricsData;
    metricsData = companyMetrics.orElseThrow(() -> new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()),
            String.format("No company metrics data found for company number: %s", companyNumber)));

    String registerMovedTo = Optional.ofNullable(metricsData)
            .map(MetricsApi::getRegisters)
            .map(RegistersApi::getPersonsWithSignificantControl)
            .map(RegisterApi::getRegisterMovedTo)
            .orElseThrow(() -> new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()),
                    String.format("company %s not on public register", companyNumber)));

      if (registerMovedTo.equals("public-register")) {
        Optional<List<PscStatementDocument>> statementListOptional = pscStatementRepository.getStatementListRegisterView(companyNumber, startIndex,
                metricsData.getRegisters().getPersonsWithSignificantControl().getMovedOn(), itemsPerPage);
        List<PscStatementDocument> pscStatementDocuments = statementListOptional.filter(docs -> !docs.isEmpty()).orElseThrow(() ->
                new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()), String.format(
                        "Resource not found for company number: %s", companyNumber)));

        return createStatementList(pscStatementDocuments, startIndex, itemsPerPage, companyMetrics, companyNumber, true);
      } else {
        throw new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()), String.format("company %s not on public register", companyNumber));
      }
  }

  public void deletePscStatement(String contextId, String companyNumber, String statementId, String requestDeltaAt) {
    if (StringUtils.isBlank(requestDeltaAt)){
      throw new BadRequestException("deltaAt missing from delete request");
    }
    try {
      Optional<PscStatementDocument> pscStatementDocument = getPscStatementDocument(companyNumber, statementId);

      pscStatementDocument.ifPresentOrElse(doc -> {
        String existingDeltaAt = doc.getDeltaAt();
        if (isDeltaStale(requestDeltaAt, existingDeltaAt)) {
          throw new ConflictException(
                  String.format("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]",
                          requestDeltaAt, existingDeltaAt));
        }

        pscStatementRepository.delete(doc);
        logger.infoContext(contextId,
                String.format("Psc Statement is deleted in MongoDb with companyNumber %s and statementId %s",
                        companyNumber, statementId), DataMapHolder.getLogMap());
        apiClientService.invokeChsKafkaApiDelete(new ResourceChangedRequest(contextId, companyNumber, statementId, doc, true));
      }, () -> {
        logger.infoContext(contextId,
                String.format("PSC Statement does not exist for companyNumber %s and statementId %s", companyNumber, statementId), DataMapHolder.getLogMap());
        apiClientService.invokeChsKafkaApiDelete(new ResourceChangedRequest(contextId, companyNumber, statementId, new PscStatementDocument(), true));
      });
    } catch (DataAccessException ex) {
      logger.error("Error connecting to MongoDB", ex);
      throw new ServiceUnavailableException("Error connecting to MongoDB");
    }
  }

  private Optional<PscStatementDocument> getPscStatementDocument(String companyNumber, String statementId) {
      return pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
  }

  public void processPscStatement(String contextId, String companyNumber, String statementId,
                                  CompanyPscStatement companyPscStatement) throws BadRequestException {
    boolean isLatestRecord = isLatestRecord(companyNumber, statementId, companyPscStatement.getDeltaAt());

    if (isLatestRecord) {
      PscStatementDocument document = pscStatementTransformer.transformPscStatement(companyNumber, statementId, companyPscStatement);

      saveToDb(contextId, companyNumber, statementId, document);
      apiClientService.invokeChsKafkaApi(new ResourceChangedRequest(contextId, companyNumber, statementId, null, false));
    } else {
      logger.infoContext(contextId, "Psc Statement not persisted as the record provided is not the latest record.", DataMapHolder.getLogMap());
    }
  }

  private boolean isLatestRecord(String companyNumber, String statementId, String deltaAt) {
    Optional<PscStatementDocument> statement;
    if(StringUtils.isBlank(deltaAt)) {
      statement = pscStatementRepository.findById(statementId).filter(doc -> !StringUtils.isBlank(doc.getDeltaAt()));
    } else {
      statement = pscStatementRepository.findUpdatedPscStatement(companyNumber, statementId, deltaAt);
    }
    return statement.isEmpty();
  }

  private void saveToDb(String contextId, String companyNumber, String statementId, PscStatementDocument document) {

    Created created = getCreatedFromCurrentRecord(companyNumber, statementId);
    if(created == null) {
      document.setCreated(new Created().setAt(document.getUpdated().getAt()));
    } else {
      document.setCreated(created);
    }

    try {
      pscStatementRepository.save(document);
      logger.infoContext(contextId, String.format(
              "Psc statement is updated in MongoDb for context id: %s, company number: %s, and statement id: %s",
              contextId, companyNumber, statementId), DataMapHolder.getLogMap());
    } catch (IllegalArgumentException illegalArgumentEx) {
      throw new BadRequestException("Saving to MongoDb failed", illegalArgumentEx);
    }

  }

  private Created getCreatedFromCurrentRecord(String companyNumber,String statementId) {
    Optional<PscStatementDocument> document = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    return document.map(PscStatementDocument::getCreated).orElse(null);
  }

  private StatementList createStatementList(List < PscStatementDocument > statementDocuments,
                                            int startIndex, int itemsPerPage, Optional<MetricsApi> companyMetrics, String companyNumber, boolean registerView) {

    StatementList statementList = new StatementList();
    StatementLinksType links = new StatementLinksType();
    links.setSelf(String.format("/company/%s/persons-with-significant-control-statements", companyNumber));
    if (hasActiveExemptions(companyNumber)) {
        links.setExemptions(String.format("/company/%s/exemptions", companyNumber));
    }

    List < Statement > statements = statementDocuments.stream().map(PscStatementDocument::getData).collect(Collectors.toList());

    companyMetrics.ifPresentOrElse(metricsApi -> {
      try {
        if (registerView) {
          Long withdrawnCount = statements.stream()
                  .filter(statement -> statement.getCeasedOn() != null).count();

          statementList.setCeasedCount(withdrawnCount.intValue());
          statementList.setTotalResults(metricsApi.getCounts().getPersonsWithSignificantControl().getActiveStatementsCount()
                  + statementList.getCeasedCount());
          statementList.setActiveCount(metricsApi.getCounts().getPersonsWithSignificantControl().getActiveStatementsCount());
        } else {
          statementList.setActiveCount(metricsApi.getCounts().getPersonsWithSignificantControl().getActiveStatementsCount());
          statementList.setCeasedCount(metricsApi.getCounts().getPersonsWithSignificantControl().getWithdrawnStatementsCount());
          statementList.setTotalResults(metricsApi.getCounts().getPersonsWithSignificantControl().getStatementsCount());
        }
      } catch (NullPointerException exp) {
        logger.error(String.format("No PSC data in metrics for company number %s",
                companyNumber), DataMapHolder.getLogMap());
      }
      }, () -> {
      logger.info(String.format("No company metrics counts data found for company number: %s",
              companyNumber), DataMapHolder.getLogMap());
    });

    statementList.setItemsPerPage(itemsPerPage);
    statementList.setLinks(links);
    statementList.setStartIndex(startIndex);

    statementList.setItems(statements);
    return statementList;
  }

  private boolean hasActiveExemptions(String companyNumber) {
    Optional<CompanyExemptions> companyExemptions = companyExemptionsApiService.getCompanyExemptions(companyNumber);

    return companyExemptions.filter(x ->
            x.getExemptions() != null &&
                    ((x.getExemptions().getPscExemptAsSharesAdmittedOnMarket()!= null &&
                            x.getExemptions().getPscExemptAsSharesAdmittedOnMarket().getItems().stream()
                                    .anyMatch(e -> e.getExemptTo()==null && e.getExemptFrom() != null)) ||
                            (x.getExemptions().getPscExemptAsTradingOnEuRegulatedMarket() != null &&
                                    x.getExemptions().getPscExemptAsTradingOnEuRegulatedMarket().getItems().stream()
                                            .anyMatch(e -> e.getExemptTo()==null && e.getExemptFrom() != null)) ||
                            (x.getExemptions().getPscExemptAsTradingOnRegulatedMarket() != null &&
                                    x.getExemptions().getPscExemptAsTradingOnRegulatedMarket().getItems().stream()
                                            .anyMatch(e -> e.getExemptTo()==null && e.getExemptFrom() != null)) ||
                            (x.getExemptions().getPscExemptAsTradingOnUkRegulatedMarket() != null &&
                                    x.getExemptions().getPscExemptAsTradingOnUkRegulatedMarket().getItems().stream()
                                            .anyMatch(e -> e.getExemptTo()==null && e.getExemptFrom() != null
                                            )))).isPresent();
  }

}
