package uk.gov.companieshouse.pscstatementdataapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.PscStatementApiService;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.model.Created;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import uk.gov.companieshouse.pscstatementdataapi.transform.DateTransformer;
import uk.gov.companieshouse.pscstatementdataapi.transform.PscStatementTransformer;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PscStatementService {

  @Autowired
  private Logger logger;
  @Autowired
  PscStatementRepository pscStatementRepository;
  @Autowired
  PscStatementTransformer pscStatementTransformer;
  @Autowired
  DateTransformer dateTransformer;
  @Autowired
  CompanyMetricsApiService companyMetricsApiService;

  @Autowired
  InternalApiClient internalApiClient;
  @Autowired
  PscStatementApiService apiClientService;

  public Statement retrievePscStatementFromDb(String companyNumber, String statementId) throws JsonProcessingException, ResourceNotFoundException {
    PscStatementDocument pscStatementDocument = getPscStatementDocument(companyNumber, statementId);
    return pscStatementDocument.getData();
  }

  public StatementList retrievePscStatementListFromDb(String companyNumber, int startIndex, boolean registerView, int itemsPerPage) {

    Optional<MetricsApi> companyMetrics = companyMetricsApiService.getCompanyMetrics(companyNumber);

    if (registerView) {
      return retrievePscStatementListFromDbRegisterView(companyNumber, companyMetrics, startIndex, itemsPerPage);
    }

    Optional<List<PscStatementDocument>> statementListOptional = pscStatementRepository.getStatementList(companyNumber, startIndex, itemsPerPage);
    List<PscStatementDocument> pscStatementDocuments = statementListOptional.filter(docs -> !docs.isEmpty()).orElseThrow(() ->
            new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                    "Resource not found for company number: %s", companyNumber)));

    return createStatementList(pscStatementDocuments, startIndex, itemsPerPage, companyMetrics, companyNumber, registerView);
  }

  public StatementList retrievePscStatementListFromDbRegisterView(String companyNumber, Optional<MetricsApi> companyMetrics, int startIndex, int itemsPerPage) {

    logger.info(String.format("In register view for company number: %s", companyNumber));
    MetricsApi metricsData;
      try {
        metricsData = companyMetrics.get();
      } catch (NoSuchElementException ex) {
        throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                String.format("No company metrics data found for company number: %s", companyNumber));
      }

    String registerMovedTo = Optional.ofNullable(metricsData)
            .map(MetricsApi::getRegisters)
            .map(RegistersApi::getPersonsWithSignificantControl)
            .map(RegisterApi::getRegisterMovedTo)
            .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    String.format("company %s not on public register", companyNumber)));

      if (registerMovedTo.equals("public-register")) {
        Optional<List<PscStatementDocument>> statementListOptional = pscStatementRepository.getStatementListRegisterView(companyNumber, startIndex,
                metricsData.getRegisters().getPersonsWithSignificantControl().getMovedOn(), itemsPerPage);
        List<PscStatementDocument> pscStatementDocuments = statementListOptional.filter(docs -> !docs.isEmpty()).orElseThrow(() ->
                new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                        "Resource not found for company number: %s", companyNumber)));

        return createStatementList(pscStatementDocuments, startIndex, itemsPerPage, companyMetrics, companyNumber, true);
      } else {
        throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format("company %s not on public register", companyNumber));
      }
  }


  public void deletePscStatement(String contextId, String companyNumber, String statementId) throws ResourceNotFoundException{
    PscStatementDocument pscStatementDocument = getPscStatementDocument(companyNumber, statementId);

    Statement statement = pscStatementDocument.getData();
    apiClientService.invokeChsKafkaApiWithDeleteEvent(contextId, companyNumber, statementId, statement);

    pscStatementRepository.delete(pscStatementDocument);
    logger.info(String.format("Psc Statement is deleted in MongoDb with companyNumber %s and statementId %s", companyNumber, statementId));
  }

  private PscStatementDocument getPscStatementDocument(String companyNumber, String statementId) throws ResourceNotFoundException{
    Optional<PscStatementDocument> statementOptional = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    return statementOptional.orElseThrow(() ->
            new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                    "Resource not found for statement ID: %s, and company number: %s", statementId, companyNumber)));
  }

  public void processPscStatement(String contextId, String companyNumber, String statementId,
                                  CompanyPscStatement companyPscStatement) {

    boolean isLatestRecord = isLatestRecord(companyNumber, statementId, companyPscStatement.getDeltaAt());

    if (isLatestRecord) {

      apiClientService.invokeChsKafkaApi(contextId, companyNumber, statementId);

      PscStatementDocument document = pscStatementTransformer.transformPscStatement(companyNumber, statementId, companyPscStatement);

      saveToDb(contextId, companyNumber, statementId, document);
    } else {
      logger.info("Psc Statement not persisted as the record provided is not the latest record.");
    }
  }

  private boolean isLatestRecord(String companyNumber, String statementId, String deltaAt) {
    List<PscStatementDocument> statements = pscStatementRepository.findUpdatedPscStatement(companyNumber, statementId, dateTransformer.transformDate(deltaAt));
    return statements.isEmpty();
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
      logger.info(String.format("Psc statement is updated in MongoDb for context id: %s, company number: %s, and statement id: %s",
              contextId, companyNumber, statementId));
    } catch (IllegalArgumentException illegalArgumentEx) {
      throw new BadRequestException("Saving to MongoDb failed", illegalArgumentEx);
    }

  }

  private Created getCreatedFromCurrentRecord(String companyNumber,String statementId) {
    Optional<PscStatementDocument> document = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    return document.isPresent() ? document.get().getCreated(): null;
  }

  private StatementList createStatementList(List < PscStatementDocument > statementDocuments,
                                            int startIndex, int itemsPerPage, Optional<MetricsApi> companyMetrics, String companyNumber, boolean registerView) {

    StatementList statementList = new StatementList();
    StatementLinksType links = new StatementLinksType();
    links.setSelf(String.format("/company/%s/persons-with-significant-control-statements", companyNumber));

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
        logger.error(String.format("No PSC data in metrics for company number %s", companyNumber));
      }
      }, () -> {
      logger.info(String.format("No company metrics counts data found for company number: %s", companyNumber));
    });

    statementList.setItemsPerPage(itemsPerPage);
    statementList.setLinks(links);
    statementList.setStartIndex(startIndex);

    statementList.setItems(statements);
    return statementList;
  }
}
