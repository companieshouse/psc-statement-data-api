package uk.gov.companieshouse.pscstatementdataapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.model.Created;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.companieshouse.pscstatementdataapi.transform.PscStatementTransformer;

import java.util.List;
import java.util.Optional;
@Service
public class PscStatementService {

  @Autowired
  private Logger logger;
  @Autowired
  PscStatementRepository pscStatementRepository;
  @Autowired
  PscStatementTransformer pscStatementTransformer;

  public Statement retrievePscStatementFromDb(String companyNumber, String statementId) throws JsonProcessingException, ResourceNotFoundException {
    Optional<PscStatementDocument> statementOptional = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    PscStatementDocument pscStatementDocument = statementOptional.orElseThrow(() ->
      new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                "Resource not found for statement ID: %s, and company number: %s", statementId, companyNumber)));
    return pscStatementDocument.getData();
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
    List<PscStatementDocument> statements = pscStatementRepository.findUpdatedPscStatement(companyNumber, statementId, deltaAt);
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

}
