package uk.gov.companieshouse.pscstatementdataapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.config.AbstractMongoConfig;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.Updated;

@Testcontainers
@DataMongoTest
class RepositoryITest extends AbstractMongoConfig {

    @Autowired
    private PscStatementRepository pscStatementRepository;

    private static final String COMPANY_NUMBER = "companyNumber";
    private static final String DELTA_AT = "20180101093435661593";
    private static final String STALE_DELTA_AT = "20170101093435661593";
    private static final String NEW_DELTA_AT = "20190101093435661593";

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @BeforeEach
    void setupForEach() {
        this.pscStatementRepository.deleteAll();
    }

    @Test
    void should_save_and_retrieve_psc_statements() {
        PscStatementDocument document = createPscStatementDocument("12345");

        pscStatementRepository.save(document);
        assertThat(pscStatementRepository.findById("12345")).isNotEmpty();
    }

    @Test
    void statement_list_should_return_correctly_based_on_parameters() throws NoSuchElementException {
        pscStatementRepository.save(createPscStatementDocument("1"));
        pscStatementRepository.save(createPscStatementDocument("2"));
        pscStatementRepository.save(createPscStatementDocument("3"));
        pscStatementRepository.save(createPscStatementDocument("4"));
        pscStatementRepository.save(createPscStatementDocument("5"));

        assertEquals(5, pscStatementRepository.getStatementList(
                COMPANY_NUMBER, 0, 5).size());
        assertEquals(2, pscStatementRepository.getStatementList(
                COMPANY_NUMBER, 0, 2).size());
        assertEquals(1, pscStatementRepository.getStatementList(
                COMPANY_NUMBER, 4, 5).size());
        assertEquals(0, pscStatementRepository.getStatementList(
                "Bad Company Number", 0, 5).size());
    }

    @Test
    void find_updated_returns_document_if_delta_is_stale() {
        PscStatementDocument newDocument = createPscStatementDocument("1");

        pscStatementRepository.save(newDocument);

        assertThat(pscStatementRepository.findUpdatedPscStatement(COMPANY_NUMBER, "1", STALE_DELTA_AT)).isNotEmpty();
    }

    @Test
    void find_updated_returns_empty_if_delta_is_the_same() {
        PscStatementDocument newDocument = createPscStatementDocument("1");

        pscStatementRepository.save(newDocument);

        assertThat(pscStatementRepository.findUpdatedPscStatement(COMPANY_NUMBER, "1", DELTA_AT)).isEmpty();
    }

    @Test
    void find_updated_returns_empty_if_delta_is_newer() {
        PscStatementDocument newDocument = createPscStatementDocument("1");

        pscStatementRepository.save(newDocument);

        assertThat(pscStatementRepository.findUpdatedPscStatement(COMPANY_NUMBER, "1", NEW_DELTA_AT)).isEmpty();
    }


    private PscStatementDocument createPscStatementDocument(String statementId) {
        PscStatementDocument document = new PscStatementDocument();
        document.setId(statementId);
        document.setCompanyNumber(COMPANY_NUMBER);
        document.setData(new Statement());
        Updated updated = new Updated();
        updated.setAt(LocalDateTime.of(2019, 1, 1, 1, 1));
        document.setUpdated(updated);
        document.setDeltaAt(DELTA_AT);

        return document;
    }
}
