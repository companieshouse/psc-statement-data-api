package uk.gov.companieshouse.pscstatementdataapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;

import java.util.List;

public interface PscStatementRepository extends MongoRepository<PscStatementDao, String> {

    @Query("{'company_number' : ?0, '_id' : ?1}")
    List<PscStatementDao> getPscStatementByCompanyNumberAndStatementId(String companyNumber, String statementId);
    }

