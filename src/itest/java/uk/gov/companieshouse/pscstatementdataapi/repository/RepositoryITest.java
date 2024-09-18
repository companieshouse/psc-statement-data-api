package uk.gov.companieshouse.pscstatementdataapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import uk.gov.companieshouse.pscstatementdataapi.config.AbstractMongoConfig;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.model.PscStatementDocument;
import uk.gov.companieshouse.api.model.Updated;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Testcontainers
@DataMongoTest
public class RepositoryITest extends AbstractMongoConfig {

    @Autowired
    private PscStatementRepository pscStatementRepository;

    private final String COMPANY_NUMBER = "companyNumber";

    public static final String DELTA_AT = "20180101093435661593";

    public static final String STALE_DELTA_AT = "20170101093435661593";

    public static final String NEW_DELTA_AT = "20190101093435661593";

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

        assertThat(pscStatementRepository.getStatementList(COMPANY_NUMBER, 0, 5).get().size()).isEqualTo(5);
        assertThat(pscStatementRepository.getStatementList(COMPANY_NUMBER, 0, 2).get().size()).isEqualTo(2);
        assertThat(pscStatementRepository.getStatementList(COMPANY_NUMBER, 4, 5).get().size()).isEqualTo(1);
        assertThat(pscStatementRepository.getStatementList("Bad Company Number", 0, 5).get().size()).isEqualTo(0);
    }

    @Test
    void find_updated_should_return_correct_statement() {
        PscStatementDocument newDocument = createPscStatementDocument("1");

        pscStatementRepository.save(newDocument);

        assertThat(pscStatementRepository.findUpdatedPscStatement(COMPANY_NUMBER, "1", STALE_DELTA_AT)).isNotEmpty();
    }

    @Test
    void find_updated_psc_statement_does_not_return_doc_if_same_delta_at() {
        PscStatementDocument newDocument = createPscStatementDocument("1");

        pscStatementRepository.save(newDocument);

        assertThat(pscStatementRepository.findUpdatedPscStatement(COMPANY_NUMBER, "1", DELTA_AT)).isEmpty();
    }

    @Test
    void find_updated_psc_statement_does_not_return_doc_if_new_delta_at() {
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
