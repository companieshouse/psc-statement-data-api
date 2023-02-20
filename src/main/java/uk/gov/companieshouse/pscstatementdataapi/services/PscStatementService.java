package uk.gov.companieshouse.pscstatementdataapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
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

  public StatementList retrievePscStatementListFromDb(String companyNumber, int startIndex, int itemsPerPage) throws JsonProcessingException, ResourceNotFoundException {
    Optional<List<PscStatementDocument>> statementListOptional =
            pscStatementRepository.getStatementList(companyNumber, startIndex, itemsPerPage);

    List<PscStatementDocument> pscStatementDocuments = statementListOptional.orElseThrow(() ->
            new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                    "Resource not found for company number: %s", companyNumber)));

    Optional<MetricsApi> companyMetrics = companyMetricsApiService.getCompanyMetrics(companyNumber);

    return createStatementList(pscStatementDocuments, startIndex, itemsPerPage, companyMetrics, companyNumber);
  }


  public void deletePscStatement(String contextId, String companyNumber, String statementId) throws ResourceNotFoundException{
    PscStatementDocument pscStatementDocument = getPscStatementDocument(companyNumber, statementId);

    Statement statement = pscStatementDocument.getData();
    ApiResponse<Void> apiResponse = apiClientService.invokeChsKafkaApiWithDeleteEvent(contextId, companyNumber, statementId, statement);

    logger.info(String.format("ChsKafka api DELETED invoked successfully for companyNumber %s and statementId %s", companyNumber, statementId));
    if (apiResponse == null) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, " error response received from ChsKafkaApi");
    }
    HttpStatus statusCode = HttpStatus.valueOf(apiResponse.getStatusCode());
    if (!statusCode.is2xxSuccessful()) {
      throw new ResponseStatusException(HttpStatus.valueOf(apiResponse.getStatusCode()), " error response received from ChsKafkaApi");
    }

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
    return document.map(PscStatementDocument::getCreated).orElse(null);
  }

  private StatementList createStatementList(List < PscStatementDocument > statementDocuments,
                                            int startIndex, int itemsPerPage, Optional < MetricsApi > metrics, String companyNumber) {
    StatementList statementList = new StatementList();
    List < Statement > statements = statementDocuments.stream().map(PscStatementDocument::getData).collect(Collectors.toList());
    statementList.setItemsPerPage(itemsPerPage);
    statementList.setStartIndex(startIndex);
    metrics.ifPresentOrElse(metricsApi -> {
              statementList.setActiveCount(metricsApi.getCounts().getPersonsWithSignificantControl().getActiveStatementsCount());
              statementList.setCeasedCount(metricsApi.getCounts().getPersonsWithSignificantControl().getWithdrawnStatementsCount());
              statementList.setTotalResults(metricsApi.getCounts().getPersonsWithSignificantControl().getTotalCount());
            },
            () -> {
              logger.info(String.format("No company metrics data found for company number: %s", companyNumber));
            });
    statementList.setItems(statements);
    return statementList;
  }

}
