package uk.gov.companieshouse.pscstatementdataapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
@Service
public class PscStatementService {

  @Autowired
  PscStatementRepository pscStatementRepository;

  public Statement retrievePscStatementFromDb(String companyNumber, String statementId) throws JsonProcessingException, ResourceNotFoundException {
    PscStatementDocument pscStatementDocument = getPscStatementDocument(companyNumber, statementId);
    return pscStatementDocument.getData();
  }

  public void deletePscStatement(String companyNumber, String statementId) throws ResourceNotFoundException{
    PscStatementDocument pscStatementDocument = getPscStatementDocument(companyNumber, statementId);
    pscStatementRepository.delete(pscStatementDocument);
  }

  private PscStatementDocument getPscStatementDocument(String companyNumber, String statementId) throws ResourceNotFoundException{
    Optional<PscStatementDocument> statementOptional = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    return statementOptional.orElseThrow(() ->
            new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                    "Resource not found for statement ID: %s, and company number: %s", statementId, companyNumber)));
  }
}
