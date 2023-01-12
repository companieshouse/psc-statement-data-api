package uk.gov.companieshouse.pscstatementdataapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
@Service
public class RetrievePscStatementService {

  @Autowired
  PscStatementRepository pscStatementRepository;

  public Statement retrievePscStatementFromDb(String companyNumber, String statementId) throws JsonProcessingException {
    Optional<PscStatementDao> statementOptional = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    PscStatementDao pscStatementDao = statementOptional.orElseThrow(() ->
      new IllegalArgumentException(String.format(
                "Resource not found for statement ID: %s, and company number: %f", statementId, companyNumber)));
    return pscStatementDao.getData();
  }

}
