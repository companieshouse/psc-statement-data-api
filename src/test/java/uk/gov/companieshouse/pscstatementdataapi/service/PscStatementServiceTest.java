package uk.gov.companieshouse.pscstatementdataapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.exemptions.Exemptions;
import uk.gov.companieshouse.api.exemptions.PscExemptAsSharesAdmittedOnMarketItem;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnEuRegulatedMarketItem;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnRegulatedMarketItem;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.PscStatementApiService;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.model.Created;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.Updated;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;
import uk.gov.companieshouse.pscstatementdataapi.transform.DateTransformer;
import uk.gov.companieshouse.pscstatementdataapi.transform.PscStatementTransformer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PscStatementServiceTest {

    private static final String CONTEXT_ID = TestHelper.X_REQUEST_ID;
    private static final String STATEMENT_ID = TestHelper.PSC_STATEMENT_ID;
    private static final String COMPANY_NUMBER = TestHelper.COMPANY_NUMBER;
    private static final String DELTA_AT = TestHelper.DELTA_AT;

    @Mock
    private Logger logger;

    @Mock
    PscStatementRepository repository;
    @Mock
    PscStatementTransformer statementTransformer;
    @Mock
    CompanyMetricsApiService companyMetricsApiService;
    @Mock
    PscStatementApiService apiClientService;
    @Mock
    CompanyExemptionsApiService companyExemptionsApiService;
    @Spy
    DateTransformer dateTransformer;

    @Spy
    @InjectMocks
    PscStatementService pscStatementService;

    private TestHelper testHelper;
    private Statement statement;
    private CompanyPscStatement companyPscStatement;
    private PscStatementDocument document;
    private PscExemptAsSharesAdmittedOnMarketItem pscExemptAsSharesAdmittedOnMarketItem;
    private PscExemptAsTradingOnEuRegulatedMarketItem pscExemptAsTradingOnEuRegulatedMarketItem;
    private PscExemptAsTradingOnRegulatedMarketItem pscExemptAsTradingOnRegulatedMarketItem;
    private PscExemptAsTradingOnUkRegulatedMarketItem pscExemptAsTradingOnUkRegulatedMarketItem;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper();
        statement = testHelper.createStatement();
        document = testHelper.createEmptyPscStatementDocument();
        companyPscStatement = testHelper.createCompanyPscStatement();
        pscExemptAsSharesAdmittedOnMarketItem = new PscExemptAsSharesAdmittedOnMarketItem();
        pscExemptAsTradingOnEuRegulatedMarketItem = new PscExemptAsTradingOnEuRegulatedMarketItem();
        pscExemptAsTradingOnRegulatedMarketItem = new PscExemptAsTradingOnRegulatedMarketItem();
        pscExemptAsTradingOnUkRegulatedMarketItem = new PscExemptAsTradingOnUkRegulatedMarketItem();
    }
    @Test
    void statementReturnedByCompanyNumberAndStatementIdFromRepository() throws JsonProcessingException, ResourceNotFoundException {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        Optional<PscStatementDocument> pscStatementOptional = Optional.of(document);
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(),anyString())).thenReturn(pscStatementOptional);

        Statement statement = pscStatementService.retrievePscStatementFromDb(COMPANY_NUMBER,STATEMENT_ID);

        assertEquals(expectedStatement, statement);
        verify(pscStatementService, times(1)).retrievePscStatementFromDb(COMPANY_NUMBER, STATEMENT_ID);
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
    }

    @Test
    void statementListReturnedByCompanyNumberFromRepository() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementList();
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetrics()));
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, false,25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, false, 25);
        verify(repository, times(1)).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void statementListReturnedByCompanyNumberFromRepositoryWithExemptions() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListWithExemptions();
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetrics()));
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));
        when(companyExemptionsApiService.getCompanyExeptions(any())).thenReturn(Optional.ofNullable(testHelper.createExemptions()));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, false,25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, false, 25);
        verify(repository, times(1)).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoStatementsExistGetStatementListShouldThrow() throws JsonProcessingException{

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(new ArrayList<PscStatementDocument>()));
        assertThrows(ResourceNotFoundException.class, ()-> pscStatementService.retrievePscStatementListFromDb( COMPANY_NUMBER, 0, false,25));
    }

    @Test
    void statementListReturnedForCompanyNumberButNoMetricsFound_ShouldReturnList() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListNoMetrics();
        document.setData(expectedStatement);

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, false, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, false, 25);
        verify(repository, times(1)).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void statementListReturnedByCompanyNumberFromRepositoryRegisterView() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListRegisterView();
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetricsRegisterView()));
        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, true,25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, true, 25);
        verify(repository, times(1)).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void statementListIncWithdrawnCountOneReturnedByCompanyNumberFromRepositoryRegisterView() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        expectedStatement.setCeasedOn(LocalDate.parse("2022-12-20"));
        StatementList expectedStatementList = testHelper.createStatementList();
        List<Statement> statement = (List<Statement>) expectedStatementList.getItems();
        statement.get(0).setCeasedOn(LocalDate.parse("2022-12-20"));
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetricsRegisterView()));
        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, true,25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, true, 25);
        verify(repository, times(1)).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void whenNoMetricsDataFoundForCompanyInRegisterViewShouldThrow() throws ResourceNotFoundException, IOException {
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> {
            pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, true,25);
        });

        String expectedMessage = "No company metrics data found for company number: companyNumber";
        String actualMessage = ex.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, true, 25);
        verify(repository, times(0)).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void whenCompanyNotInPublicRegisterGetStatementListShouldThrow() throws ResourceNotFoundException, IOException {
        MetricsApi metricsApi = testHelper.createMetrics();
        RegistersApi registersApi = new RegistersApi();
        metricsApi.setRegisters(registersApi);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(metricsApi));

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> {
            pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, true,25);
        });

        String expectedMessage = "company companyNumber not on public register";
        String actualMessage = ex.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, true, 25);
        verify(repository, times(0)).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void whenNoStatementsExistInPublicRegisterGetStatementListRegisterViewShouldThrow() throws ResourceNotFoundException, IOException {
        MetricsApi metricsApi = testHelper.createMetrics();
        RegistersApi registersApi = new RegistersApi();
        RegisterApi api = new RegisterApi();
        api.setMovedOn(OffsetDateTime.parse("2022-12-20T06:00Z"));
        api.setRegisterMovedTo("public-register");
        registersApi.setPersonsWithSignificantControl(api);
        metricsApi.setRegisters(registersApi);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(metricsApi));
        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(Optional.of(new ArrayList<PscStatementDocument>()));

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> {
            pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, true,25);
        });

        String expectedMessage = "Resource not found for company number: companyNumber";
        String actualMessage = ex.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, true, 25);
        verify(repository, times(1)).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2022-12-20T06:00Z"), 25);
    }

    @Test
    void whenNoCountsDataInCompanyMetricsGetStatementListRegisterViewShouldReturnList() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListNoMetrics();
        expectedStatementList.setCeasedCount(0);
        document.setData(expectedStatement);
        MetricsApi metricsApi = testHelper.createMetricsRegisterView();
        metricsApi.setCounts(null);

        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(metricsApi));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, true, 25);

        assertEquals(expectedStatementList, statementList);
        assertEquals(statementList.getActiveCount(), null);
        assertEquals(statementList.getTotalResults(), null);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, true, 25);
        verify(repository, times(1)).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void deletePscStatementDeletesStatement() {
        ApiResponse<Void> response = new ApiResponse<>(200, null);
        document.setData(statement);
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        when(apiClientService.invokeChsKafkaApiWithDeleteEvent(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID, statement)).thenReturn(response);

        pscStatementService.deletePscStatement(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID);

        verify(repository).delete(document);
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
    }
    @Test
    void deleteThrowsExceptionWhenInvalidIdGiven() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        assertThrows(ResourceNotFoundException.class, () -> pscStatementService.deletePscStatement(CONTEXT_ID, COMPANY_NUMBER, "invalid_id"));
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, "invalid_id");
    }
    @Test
    void deleteThrowsExceptionWhenInvalidCompanyNumberGiven() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        assertThrows(ResourceNotFoundException.class, () -> pscStatementService.deletePscStatement(CONTEXT_ID, "invalid_company_no", STATEMENT_ID));
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId("invalid_company_no", STATEMENT_ID);
    }

    @Test
    void processPscStatementSavesStatement() {
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(document);

        pscStatementService.processPscStatement("", COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        verify(repository).save(document);
        verify(repository, times(1)).findUpdatedPscStatement(eq(COMPANY_NUMBER),eq(STATEMENT_ID), any());
        verify(dateTransformer, times(1)).transformDate(any());
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    void processPscStatementUpdatesStatement() {
        LocalDateTime dateTime = LocalDateTime.now();
        Created created = new Created();
        created.setAt(dateTime);
        document.setCreated(created);
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        ApiResponse<Void> response = new ApiResponse<>(200, null);
        when(apiClientService.invokeChsKafkaApi(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID)).thenReturn(response);
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(document);

        pscStatementService.processPscStatement("", COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        verify(repository).save(document);
        verify(repository, times(1)).findUpdatedPscStatement(eq(COMPANY_NUMBER),eq(STATEMENT_ID), any());
        verify(dateTransformer, times(1)).transformDate(any());
        assertEquals(document.getCreated().getAt(), dateTime);
    }

    @Test
    void processPscStatementDoesntUpdateStatementWhenDeltaAtInPast() {
        LocalDateTime dateTime = LocalDateTime.now();
        Updated updated = new Updated();
        updated.setAt(dateTime);
        document.setUpdated(updated);
        when(repository.findUpdatedPscStatement(COMPANY_NUMBER, STATEMENT_ID, dateTransformer.transformDate(DELTA_AT))).thenReturn(Arrays.asList(document));
        ApiResponse<Void> response = new ApiResponse<>(200, null);
        when(apiClientService.invokeChsKafkaApi(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID)).thenReturn(response);
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(document);

        pscStatementService.processPscStatement("", COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        verify(repository, times(0)).save(document);
        verify(repository, times(1)).findUpdatedPscStatement(eq(COMPANY_NUMBER),eq(STATEMENT_ID), any());
        assertEquals(document.getUpdated().getAt(), dateTime);
    }

    @Test
    void hasPscExemptionsReturnsTrueWhenSharesAdmittedOnMarket() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsSharesAdmittedOnMarket(pscExemptAsSharesAdmittedOnMarketItem);
        companyExemptions.setExemptions(exemptions);
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER)).thenReturn(optionalExempt);
        
        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");
        linksType.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void hasPscExemptionsReturnsTrueWhenTradingOnEuRegulatedMarket() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnEuRegulatedMarket(pscExemptAsTradingOnEuRegulatedMarketItem);
        companyExemptions.setExemptions(exemptions);
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER)).thenReturn(optionalExempt);
        
        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");
        linksType.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void hasPscExemptionsReturnsTrueWhenTradingOnRegulatedMarket() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnRegulatedMarket(pscExemptAsTradingOnRegulatedMarketItem);
        companyExemptions.setExemptions(exemptions);
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER)).thenReturn(optionalExempt);
        
        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");
        linksType.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void hasPscExemptionsReturnsTrueWhenTradingOnUkRegulatedMarket() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(pscExemptAsTradingOnUkRegulatedMarketItem);
        companyExemptions.setExemptions(exemptions);
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER)).thenReturn(optionalExempt);
        
        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");
        linksType.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void hasPscExemptionsReturnsFalseWhenNoPscExemption() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        companyExemptions.setExemptions(exemptions);
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER)).thenReturn(optionalExempt);
        
        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");

        assertEquals(list.getLinks(), linksType);
    }
}