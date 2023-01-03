package uk.gov.companieshouse.pscstatementdataapi.services;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
@Service
public class RetrievePscStatementService {

  @Autowired
  PscStatementRepository pscStatementRepository;

  public PscStatementDao retrievePscStatementFromDb(String companyNumber, String statementId) throws JsonProcessingException {
    Optional<Document> statementOptional = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    Document source = statementOptional.orElseThrow(() ->
      new IllegalArgumentException(String.format(
                "Resource not found for statement ID: %s, and company number: %f", statementId, companyNumber)));
    return this.transformPscStatement(source);
  }

  public PscStatementDao transformPscStatement(Document source) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    PscStatementDao pscStatementDao = objectMapper.readValue(source.toJson(), PscStatementDao.class);
    return pscStatementDao;
  }
}
