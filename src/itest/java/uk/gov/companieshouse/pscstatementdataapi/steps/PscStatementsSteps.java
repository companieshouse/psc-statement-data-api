package uk.gov.companieshouse.pscstatementdataapi.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.PscStatementApiService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.pscstatementdataapi.config.AbstractMongoConfig.mongoDBContainer;

import uk.gov.companieshouse.pscstatementdataapi.config.CucumberContext;
import uk.gov.companieshouse.pscstatementdataapi.exception.ServiceUnavailableException;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.util.FileReaderUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

    @Autowired
    private CompanyMetricsApiService companyMetricsApiService;

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
    public void psc_statement_exists_for_campany_and_id(String companyNumber, String statementId) throws IOException {
        File statementFile = new ClassPathResource("/json/output/psc_statement.json").getFile();
        Statement pscStatement = objectMapper.readValue(statementFile, Statement.class);

        PscStatementDocument document = new PscStatementDocument();
        document.setId(statementId);
        document.setCompanyNumber(companyNumber);
        document.setData(pscStatement);
        mongoTemplate.save(document);
        assertThat(pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId)).isNotEmpty();
    }

    @Given("Psc statements exist for company number {string}")
    public void psc_statements_exist_for_company(String companyNumber) throws IOException {
        File statementFile = new ClassPathResource("/json/output/psc_statement.json").getFile();
        Statement pscStatement = objectMapper.readValue(statementFile, Statement.class);
        PscStatementDocument document = new PscStatementDocument();
        document.setId("1");
        document.setCompanyNumber(companyNumber);
        document.setData(pscStatement);
        PscStatementDocument document2 = new PscStatementDocument();
        document2.setId("2");
        document2.setCompanyNumber(companyNumber);
        document2.setData(pscStatement);
        mongoTemplate.save(document);
        mongoTemplate.save(document2);
    }

    @Given("the database is down")
    public void the_psc_statements_db_is_down() {
        mongoDBContainer.stop();
    }


    @When("I send an GET request for company number {string} with statement id {string}")
    public void i_send_psc_statement_get_request_with_statement_id(String companyNumber, String statementId) throws IOException {
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");
        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<Statement> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                Statement.class, companyNumber, statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("I send a GET statement list request for company number {string}")
    public void get_statement_list_for_company_number(String companyNumber) throws IOException {
        String uri = "/company/{company_number}/persons-with-significant-control-statements";

        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");
        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<StatementList> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                StatementList.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }
    @Then("the psc statement Get call response body should match {string} file")
    public void the_psc_statement_get_call_response_body_should_match(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/output/" + dataFile + ".json").getFile();
        Statement expected = objectMapper.readValue(file, Statement.class);

        Statement actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getEtag()).isEqualTo(actual.getEtag());
        assertThat(expected.getKind()).isEqualTo(actual.getKind());
        assertThat(expected.getLinks()).isEqualTo(actual.getLinks());
    }

    @Then("the psc statement list Get call response body should match {string} file")
    public void the_psc_statement_list_get_call_response_body_should_match(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/output/" + dataFile + ".json").getFile();
        StatementList expected = objectMapper.readValue(file, StatementList.class);

        StatementList actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getItemsPerPage()).isEqualTo(actual.getItemsPerPage());
        assertThat(expected.getItems()).isEqualTo(actual.getItems());
        assertThat(expected.getLinks()).isEqualTo(actual.getLinks());
    }



    @When("I send a PUT request with payload {string} file for company number {string} with statement id {string}")
    public void i_send_psc_statement_put_request_with_payload(String dataFile, String companyNumber, String statementId) throws IOException {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}/internal";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, companyNumber, statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send a PUT request with no ERIC headers")
    public void i_send_psc_statement_put_request_no_eric_headers() throws IOException {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/company_psc_statement.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}/internal";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, "12345", "abcde");

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send DELETE request for company number {string} with statement id {string}")
    public void send_delete_request_for_statement(String companyNumber, String statementId) throws IOException {
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}/internal";

        HttpHeaders headers = new HttpHeaders();
        CucumberContext.CONTEXT.set("contextId", "5234234234");
        headers.set("x-request-id", CucumberContext.CONTEXT.get("contextId"));
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, companyNumber, statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("psc statement id does not exist for {string}")
    public void statement_does_not_exist(String statementId) {
        Assertions.assertThat(pscStatementRepository.existsById(statementId)).isFalse();
    }

    @When("CHS kafka API service is unavailable")
    public void chs_kafka_service_unavailable() throws IOException {
        doThrow(ServiceUnavailableException.class)
                .when(pscStatementApiService).invokeChsKafkaApiWithDeleteEvent(any(), any(), any(), any());
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() throws IOException {
        verify(pscStatementApiService, times(0)).invokeChsKafkaApi(any(), any(), any());
    }

    @Then("the CHS Kafka API delete is invoked for company number {string} with statement id {string} and the correct statement data")
    public void chs_kafka_api_invoked_delete(String companyNumber, String statementId) throws IOException {
        File statementFile = new ClassPathResource("/json/output/psc_statement.json").getFile();
        Statement pscStatement = objectMapper.readValue(statementFile, Statement.class);
        verify(pscStatementApiService).invokeChsKafkaApiWithDeleteEvent("5234234234", companyNumber, statementId, pscStatement);
    }

    @When("a statement exists with id {string}")
    public void statement_exists(String statementId) {
        Assertions.assertThat(pscStatementRepository.existsById(statementId)).isTrue();
    }

    @Then("no statement exists with id {string}")
    public void no_statement_exists(String statementId) {
        Assertions.assertThat(pscStatementRepository.existsById(statementId)).isFalse();
    }

    @Then("the CHS Kafka API is invoked for company number {string} with statement id {string}")
    public void chs_kafka_api_invoked(String companyNumber, String statementId) {
        verify(pscStatementApiService).invokeChsKafkaApi("5234234234", companyNumber, statementId);
    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_to_database() {
        List<PscStatementDocument> pscDocs = pscStatementRepository.findAll();
        Assertions.assertThat(pscDocs).hasSize(0);
    }

    @After
    public void dbStop(){
        mongoDBContainer.stop();
    }
}