package uk.gov.companieshouse.pscstatementdataapi.repository;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface PscStatementRepository extends MongoRepository<PscStatementDocument, String> {

    @Query("{'company_number' : ?0, '_id' : ?1}")
    Optional<PscStatementDocument> getPscStatementByCompanyNumberAndStatementId(String companyNumber, String statementId);


    @Query("{'company_number' : ?0, '_id': ?1, 'delta_at' : {$gte : ?2 }}")
    Optional<PscStatementDocument> findUpdatedPscStatement(String companyNumber, String statementId, String at);

    @Aggregation(pipeline = {
            "{'$match': { 'company_number': ?0} } }",
            "{'$sort': {'data.notified_on': -1, 'data.ceased_on': -1 } }",
            "{'$skip': ?1}",
            "{'$limit': ?2}",
        })
    Optional<List<PscStatementDocument>> getStatementList(String companyNumber, int startIndex, int itemsPerPage);

    @Aggregation(pipeline = {
            "{'$match': { 'company_number' : ?0, $or:[ { 'data.ceased_on': { $gte : { \"$date\" : \"?2\" }} },{ 'data.ceased_on': {$exists: false }} ]} }",
            "{'$sort': {'data.notified_on': -1, 'data.ceased_on': -1 } }",
            "{'$skip': ?1}",
            "{'$limit': ?3}",
    })
    Optional<List<PscStatementDocument>> getStatementListRegisterView(String companyNumber, int startIndex, OffsetDateTime movedOn, int itemsPerPage);
}
