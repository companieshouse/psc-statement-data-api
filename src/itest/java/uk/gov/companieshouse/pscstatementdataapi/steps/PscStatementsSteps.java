package uk.gov.companieshouse.pscstatementdataapi.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.api.PscStatementApiService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.pscstatementdataapi.config.AbstractMongoConfig.mongoDBContainer;

import uk.gov.companieshouse.pscstatementdataapi.config.CucumberContext;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;

import java.io.File;
import java.io.IOException;

public class PscStatementsSteps {
    private String contextId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PscStatementRepository pscStatementRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public PscStatementApiService pscStatementApiService;

    @Before
    public void dbCleanUp(){
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        pscStatementRepository.deleteAll();
    }

    @Given("Psc statements data api service is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
    }

    @Given("a psc statement exists for company number {string} with statement id {string}")
    public void psc_statement_exists_exists_for(String companyNumber, String statementId) throws IOException {
        File statementFile = new ClassPathResource("/json/output/psc_statement.json").getFile();
        PscStatementDocument pscStatement = objectMapper.readValue(statementFile, PscStatementDocument.class);
        pscStatement.setId(statementId);
        pscStatement.setCompanyNumber(companyNumber);
        mongoTemplate.save(pscStatement);
    }

    @When("I send an GET request for company number {string} with statement id {string}")
    public void i_send_psc_statement_get_request_with_statement_id(String companyNumber, String statementId) throws IOException {
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "KEY");
        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<Statement> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                Statement.class, companyNumber, statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }
}
