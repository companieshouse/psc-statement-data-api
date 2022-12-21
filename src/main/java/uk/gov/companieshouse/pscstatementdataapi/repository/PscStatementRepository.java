package uk.gov.companieshouse.pscstatementdataapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;

import java.util.List;
import java.util.Optional;

public interface PscStatementRepository extends MongoRepository<PscStatementDao, String> {

    @Query("{'company_number' : ?0, '_id' : ?1}")
    Optional<PscStatementDao> getPscStatementByCompanyNumberAndStatementId(String companyNumber, String statementId);
    }

