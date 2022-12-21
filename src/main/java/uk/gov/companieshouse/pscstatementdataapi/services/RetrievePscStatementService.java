package uk.gov.companieshouse.pscstatementdataapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

public class RetrievePscStatementService {

  @Autowired
  PscStatementRepository pscStatementRepository;

  public PscStatementDao retrievePscStatementFromDb(
    String companyNumber,
    String statementId
  ) throws JsonProcessingException {
    Optional<PscStatementDao> statementOptional = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(
      companyNumber,
      statementId
    );
    PscStatementDao pscStatementDao = statementOptional.orElseThrow(() ->
      new IllegalArgumentException(String.format(
                "Resource not found for statement ID: %s, and company number: %f", statementId, companyNumber))
    );
    this.transformPscStatement(pscStatementDao);
    return null;
  }

  public PscStatementDao transformPscStatement(PscStatementDao pscStatementDao)
    throws JsonProcessingException {
    String response = pscStatementDao.toString();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.readValue(response, PscStatementDao.class);
    return null;
  }
}
