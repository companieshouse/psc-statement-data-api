package uk.gov.companieshouse.pscstatementdataapi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Spy;
import uk.gov.companieshouse.pscstatementdataapi.config.AbstractMongoConfig;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.Updated;
import uk.gov.companieshouse.pscstatementdataapi.transform.DateTransformer;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class RepositoryITest extends AbstractMongoConfig {

    @Autowired
    private PscStatementRepository pscStatementRepository;

    @Spy
    DateTransformer dateTransformer;

    private final String COMPANY_NUMBER = "companyNumber";

    public static final String DELTA_AT = "20180101093435661593";


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
        Assertions.assertThat(pscStatementRepository.findById("12345")).isNotEmpty();
    }

    @Test
    void statement_list_should_return_correctly_based_on_parameters() throws NoSuchElementException {
        pscStatementRepository.save(createPscStatementDocument("1"));
        pscStatementRepository.save(createPscStatementDocument("2"));
        pscStatementRepository.save(createPscStatementDocument("3"));
        pscStatementRepository.save(createPscStatementDocument("4"));
        pscStatementRepository.save(createPscStatementDocument("5"));

        Assertions.assertThat(pscStatementRepository.getStatementList(COMPANY_NUMBER, 0, 5).get().size()).isEqualTo(5);
        Assertions.assertThat(pscStatementRepository.getStatementList(COMPANY_NUMBER, 0, 2).get().size()).isEqualTo(2);
        Assertions.assertThat(pscStatementRepository.getStatementList(COMPANY_NUMBER, 4, 5).get().size()).isEqualTo(1);
        Assertions.assertThat(pscStatementRepository.getStatementList("Bad Company Number", 0, 5).get().size()).isEqualTo(0);
    }

    @Test
    void find_updated_should_return_correct_statement() {

        PscStatementDocument newDocument = createPscStatementDocument("1");

        pscStatementRepository.save(newDocument);

        String date = dateTransformer.transformDate(DELTA_AT);

        Assertions.assertThat(pscStatementRepository.findUpdatedPscStatement(COMPANY_NUMBER, "1", date)).isNotEmpty();

    }


    private PscStatementDocument createPscStatementDocument(String statementId) {
        PscStatementDocument document = new PscStatementDocument();
        document.setId(statementId);
        document.setCompanyNumber(COMPANY_NUMBER);
        document.setData(new Statement());
        Updated updated = new Updated();
        updated.setAt(LocalDateTime.of(2019, 1, 1, 1, 1));
        document.setUpdated(updated);

        return document;
    }
}
