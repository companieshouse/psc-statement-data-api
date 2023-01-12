package uk.gov.companieshouse.pscstatementdataapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;

import java.util.Optional;
@Repository
public interface PscStatementRepository extends MongoRepository<PscStatementDocument, String> {

    @Query("{'company_number' : ?0, '_id' : ?1}")
    Optional<PscStatementDocument> getPscStatementByCompanyNumberAndStatementId(String companyNumber, String statementId);
    }
