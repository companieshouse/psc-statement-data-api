package uk.gov.companieshouse.pscstatementdataapi.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.companieshouse.api.exemptions.PscExemptAsSharesAdmittedOnMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_SHARES_ADMITTED_ON_MARKET;
import static uk.gov.companieshouse.pscstatementdataapi.config.AbstractMongoConfig.mongoDBContainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.bson.Document;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.exemptions.ExemptionItem;
import uk.gov.companieshouse.api.exemptions.Exemptions;
import uk.gov.companieshouse.api.exemptions.PscExemptAsSharesAdmittedOnMarketItem;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscstatementdataapi.config.CucumberContext;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.util.FileReaderUtil;

public class PscStatementsSteps {

    private static final LocalDate EXEMPTION_DATE = LocalDate.of(2022, 11, 3);
    private static final String PSC_STATEMENT_COLLECTION = "company_psc_statements";

    private final ObjectMapper objectMapper;
    private final TestRestTemplate restTemplate;
    private final PscStatementRepository pscStatementRepository;
    private final MongoTemplate mongoTemplate;
    private final CompanyMetricsApiService companyMetricsApiService;
    private final ChsKafkaApiService pscStatementApiService;
    private final CompanyExemptionsApiService companyExemptionsApiService;

    public PscStatementsSteps(ObjectMapper objectMapper, TestRestTemplate restTemplate,
            PscStatementRepository pscStatementRepository, MongoTemplate mongoTemplate,
            CompanyMetricsApiService companyMetricsApiService, ChsKafkaApiService chsKafkaApiService,
            CompanyExemptionsApiService companyExemptionsApiService) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.pscStatementRepository = pscStatementRepository;
        this.mongoTemplate = mongoTemplate;
        this.companyMetricsApiService = companyMetricsApiService;
        this.pscStatementApiService = chsKafkaApiService;
        this.companyExemptionsApiService = companyExemptionsApiService;
    }


    @Before
    public void dbCleanUp() {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        pscStatementRepository.deleteAll();
        MockitoAnnotations.openMocks(this);
    }

    @Given("Psc statements data api service is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
    }

    @Given("a psc statement exists for company number {string} with statement id {string}")
    public void psc_statement_exists_for_company_and_id(String companyNumber, String statementId) throws IOException {
        String statementFile = FileReaderUtil.readFile("src/itest/resources/json/output/psc_statement.json");

        Statement pscStatement = objectMapper.readValue(statementFile, Statement.class);

        PscStatementDocument document = new PscStatementDocument();
        document.setId(statementId);
        document.setCompanyNumber(companyNumber);
        document.setData(pscStatement);
        mongoTemplate.save(document);
        assertThat(pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber,
                statementId)).isNotEmpty();

        CucumberContext.CONTEXT.set("pscStatementDocument", document);
    }

    @Given("a psc statement exists for company number {string} with statement id {string} and delta_at {string}")
    public void psc_statement_exists_for_company_and_id_with_delta_at(String companyNumber, String statementId,
            String deltaAt) throws IOException {
        String statementFile = FileReaderUtil.readFile("src/itest/resources/json/output/psc_statement.json");
        Statement pscStatement = objectMapper.readValue(statementFile, Statement.class);

        PscStatementDocument document = new PscStatementDocument();
        document.setId(statementId);
        document.setCompanyNumber(companyNumber);
        document.setData(pscStatement);
        document.setDeltaAt(deltaAt);
        mongoTemplate.save(document);
        assertThat(pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber,
                statementId)).isNotEmpty();
    }

    @And("a psc statement exists with legacy data for company number {string} with statement id {string}")
    public void aPscStatementExistsWithLegacyDataForCompanyNumberWithStatementId(String companyNumber, String statementId)
            throws IOException {
        String jsonToInsert = IOUtils.resourceToString("/json/input/psc_statement_legacy.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", statementId)
                .replaceAll("<company_number>", companyNumber);
        Document document = Document.parse(jsonToInsert);
        mongoTemplate.insert(document, PSC_STATEMENT_COLLECTION);

        assertThat(pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber,
                statementId)).isNotEmpty();

        CucumberContext.CONTEXT.set("pscStatementDocument", document);
    }

    @Given("Psc statements exist for company number {string}")
    public void psc_statements_exist_for_company(String companyNumber) throws IOException {
        String statementFile = FileReaderUtil.readFile("src/itest/resources/json/output/psc_statement.json");

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
    public void i_send_psc_statement_get_request_with_statement_id(String companyNumber, String statementId)
            throws IOException {
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");
        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<Statement> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                Statement.class, companyNumber, statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("I send a GET request for company number {string} to company metrics api")
    public void i_send_get_request_to_company_metrics_api(String companyNumber) throws IOException {
        String uri = "/company/{company_number}/metrics";

        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        ResponseEntity<MetricsApi> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                MetricsApi.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
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

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("I send a GET statement list request for company number in register view {string}")
    public void get_statement_list_for_company_number_register_view(String companyNumber) throws IOException {

        String uri = "/company/{company_number}/persons-with-significant-control-statements?register_view=true";

        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");
        HttpEntity<String> request = new HttpEntity<String>(null, headers);
        ResponseEntity<StatementList> response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, request,
                    StatementList.class, companyNumber);
        } catch (Exception ex) {
            response = new ResponseEntity<>(HttpStatusCode.valueOf(NOT_FOUND.value()));
        }
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("Company Metrics API is available for company number {string}")
    public void company_metrics_api_service_available(String companyNumber) throws IOException {
        String metricsFile = FileReaderUtil.readFile(
                "src/itest/resources/json/input/company_metrics_" + companyNumber + ".json");

        MetricsApi metrics = objectMapper.readValue(metricsFile, MetricsApi.class);
        Optional<MetricsApi> metricsApi = Optional.ofNullable(metrics);

        when(companyMetricsApiService.getCompanyMetrics(any())).thenReturn(metricsApi);
    }

    @When("Company Metrics API is unavailable")
    public void company_metrics_api_service_unavailable() throws IOException {
        when(companyMetricsApiService.getCompanyMetrics(any())).thenReturn(Optional.empty());
    }

    @When("Company Exemptions API is available for company number {string}")
    public void company_exemptions_api_service_available(String companyNumber) throws IOException {
        String exemptionsFile = FileReaderUtil.readFile(
                "src/itest/resources/json/input/company_exemptions_" + companyNumber + ".json");

        CompanyExemptions companyExemptions = objectMapper.readValue(exemptionsFile, CompanyExemptions.class);
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);
        PscExemptAsSharesAdmittedOnMarketItem nonUkEeaStateMarket = new PscExemptAsSharesAdmittedOnMarketItem();
        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_SHARES_ADMITTED_ON_MARKET);

        PscExemptAsSharesAdmittedOnMarketItem ukEeaStateMarket = new PscExemptAsSharesAdmittedOnMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_SHARES_ADMITTED_ON_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsSharesAdmittedOnMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsSharesAdmittedOnMarket(ukEeaStateMarket);

        companyExemptions.setExemptions(exemptions);

        Optional<CompanyExemptions> exemptionsApi = Optional.ofNullable(companyExemptions);

        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(exemptionsApi);
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

    @Then("the psc statement Get call response body should match {string} file")
    public void the_psc_statement_get_call_response_body_should_match(String dataFile) throws IOException {
        String statementFile = FileReaderUtil.readFile("src/itest/resources/json/output/" + dataFile + ".json");

        Statement expected = objectMapper.readValue(statementFile, Statement.class);

        Statement actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getEtag()).isEqualTo(actual.getEtag());
        assertThat(expected.getKind()).isEqualTo(actual.getKind());
        assertThat(expected.getLinks()).isEqualTo(actual.getLinks());
    }

    @Then("the psc statement list Get call response body should match {string} file")
    public void the_psc_statement_list_get_call_response_body_should_match(String dataFile) throws IOException {

        String file = FileReaderUtil.readFile("src/itest/resources/json/output/" + dataFile + ".json");

        StatementList expected = objectMapper.readValue(file, StatementList.class);

        StatementList actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getCeasedCount()).isEqualTo(actual.getCeasedCount());
        assertThat(expected.getTotalResults()).isEqualTo(actual.getTotalResults());
        assertThat(expected.getItemsPerPage()).isEqualTo(actual.getItemsPerPage());
        assertThat(expected.getItems()).isEqualTo(actual.getItems());
        assertThat(expected.getLinks()).isEqualTo(actual.getLinks());
    }


    @When("I send a PUT request with payload {string} file for company number {string} with statement id {string}")
    public void i_send_psc_statement_put_request_with_payload(String dataFile, String companyNumber, String statementId)
            throws IOException {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        CucumberContext.CONTEXT.set("contextId", "5234234234");
        headers.set("x-request-id", CucumberContext.CONTEXT.get("contextId"));
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}/internal";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, companyNumber,
                statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
    }

    @When("I send a PUT request with no ERIC headers")
    public void i_send_psc_statement_put_request_no_eric_headers() throws IOException {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/company_psc_statement_put.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        CucumberContext.CONTEXT.set("contextId", "5234234234");
        headers.set("x-request-id", CucumberContext.CONTEXT.get("contextId"));

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}/internal";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, "12345",
                "abcde");

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
    }

    @When("I send DELETE request for company number {string} with statement id {string}")
    public void send_delete_request_for_statement(String companyNumber, String statementId) throws IOException {
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}/internal";

        HttpHeaders headers = new HttpHeaders();
        CucumberContext.CONTEXT.set("contextId", "5234234234");
        headers.set("x-request-id", CucumberContext.CONTEXT.get("contextId"));
        headers.set("X-DELTA-AT", "20240723093435661593");
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class,
                companyNumber, statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
    }

    @When("I send DELETE request for company number {string} with statement id {string} without delta at")
    public void send_delete_request_for_statement_without_delta_at(String companyNumber, String statementId) {
        String uri = "/company/{company_number}/persons-with-significant-control-statements/{statement_id}/internal";

        HttpHeaders headers = new HttpHeaders();
        CucumberContext.CONTEXT.set("contextId", "5234234234");
        headers.set("x-request-id", CucumberContext.CONTEXT.get("contextId"));
        headers.set("X-DELTA-AT", null);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Roles", "*");
        headers.set("X-DELTA-AT", "");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class,
                companyNumber, statementId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
    }

    @When("psc statement id does not exist for {string}")
    public void statement_does_not_exist(String statementId) {
        CucumberContext.CONTEXT.set("pscStatementDocument", new PscStatementDocument());
        assertThat(pscStatementRepository.existsById(statementId)).isFalse();
    }

    @When("CHS kafka API service is unavailable")
    public void chs_kafka_service_unavailable() throws IOException {
        doThrow(ServiceUnavailableException.class)
                .when(pscStatementApiService).invokeChsKafkaApi(any());
    }

    @And("CHS kafka API service is unavailable for delete")
    public void chsKafkaAPIServiceIsUnavailableForDelete() {
        doThrow(ServiceUnavailableException.class)
                .when(pscStatementApiService).invokeChsKafkaApiDelete(any());
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() throws IOException {
        verifyNoInteractions(pscStatementApiService);
    }

    @Then("the CHS Kafka API delete is invoked for company number {string} with statement id {string} and the correct statement data")
    public void chs_kafka_api_invoked_delete(String companyNumber, String statementId) throws IOException {
        PscStatementDocument document = CucumberContext.CONTEXT.get("pscStatementDocument");
        ResourceChangedRequest resourceChangedRequest = new ResourceChangedRequest(
                companyNumber, statementId, document, true);
        verify(pscStatementApiService).invokeChsKafkaApiDelete(resourceChangedRequest);
    }

    @When("a statement exists with id {string}")
    public void statement_exists(String statementId) {
        assertThat(pscStatementRepository.existsById(statementId)).isTrue();
    }

    @When("a statement exists with id {string} and delta_at {string}")
    public void statement_exists(String statementId, String deltaAt) throws NoSuchElementException {
        assertThat(pscStatementRepository.existsById(statementId)).isTrue();
        assertThat(pscStatementRepository.findById(statementId).get().getDeltaAt()).isEqualTo(deltaAt);
    }

    @Then("no statement exists with id {string}")
    public void no_statement_exists(String statementId) {
        assertThat(pscStatementRepository.existsById(statementId)).isFalse();
    }

    @Then("the CHS Kafka API is invoked for company number {string} with statement id {string}")
    public void chs_kafka_api_invoked(String companyNumber, String statementId) {
        verify(pscStatementApiService).invokeChsKafkaApi(
                new ResourceChangedRequest(companyNumber, statementId, null, false));
    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_to_database() {
        List<PscStatementDocument> pscDocs = pscStatementRepository.findAll();
        assertThat(pscDocs).isEmpty();
    }

    @And("the data matches {string} for company number {string} and statement id {string}")
    public void theDataMatchesForCompanyNumberAndStatementId(String resultFile, String companyNumber, String statementId)
            throws JsonProcessingException {
        Optional<PscStatementDocument> pscDoc = pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
        assertThat(pscDoc).isPresent();

        String file = FileReaderUtil.readFile("src/itest/resources/json/output/" + resultFile + ".json")
                .replaceAll("<id>", statementId)
                .replaceAll("<company_number>", companyNumber);
        PscStatementDocument expected = objectMapper.readValue(file, PscStatementDocument.class);

        assertEquals(expected.getData(), pscDoc.get().getData());
        assertEquals(expected.getCompanyNumber(), pscDoc.get().getCompanyNumber());
        assertEquals(expected.getPscStatementIdRaw(), pscDoc.get().getPscStatementIdRaw());
        assertEquals(expected.getId(), pscDoc.get().getId());
    }

    @After
    public void dbStop() {
        mongoDBContainer.stop();
    }

}
