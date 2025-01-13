package uk.gov.companieshouse.pscstatementdataapi.service;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.TransientDataAccessResourceException;
import uk.gov.companieshouse.api.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.api.exception.BadRequestException;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
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
import uk.gov.companieshouse.api.model.Created;
import uk.gov.companieshouse.api.model.Updated;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.api.PscStatementApiService;
import uk.gov.companieshouse.pscstatementdataapi.exception.ConflictException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;
import uk.gov.companieshouse.pscstatementdataapi.transform.PscStatementTransformer;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

@ExtendWith(MockitoExtension.class)
class PscStatementServiceTest {
    private static final String STATEMENT_ID = TestHelper.PSC_STATEMENT_ID_RAW;
    private static final String COMPANY_NUMBER = TestHelper.COMPANY_NUMBER;
    private static final String DELTA_AT = TestHelper.DELTA_AT;
    private static final String NOT_FOUND_STATEMENT_ID = "not_found_id";
    private static final String NOT_FOUND_COMPANY_NUMBER = "not_found_company_no";

    @Mock
    private PscStatementRepository repository;
    @Mock
    private PscStatementTransformer statementTransformer;
    @Mock
    private CompanyMetricsApiService companyMetricsApiService;
    @Mock
    private PscStatementApiService apiClientService;
    @Mock
    private CompanyExemptionsApiService companyExemptionsApiService;

    @Spy
    @InjectMocks
    private PscStatementService pscStatementService;

    private TestHelper testHelper;
    private CompanyPscStatement companyPscStatement;
    private PscStatementDocument document;
    private PscExemptAsSharesAdmittedOnMarketItem pscExemptAsSharesAdmittedOnMarketItem;
    private PscExemptAsTradingOnEuRegulatedMarketItem pscExemptAsTradingOnEuRegulatedMarketItem;
    private PscExemptAsTradingOnRegulatedMarketItem pscExemptAsTradingOnRegulatedMarketItem;
    private PscExemptAsTradingOnUkRegulatedMarketItem pscExemptAsTradingOnUkRegulatedMarketItem;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper();
        document = testHelper.createEmptyPscStatementDocument();
        companyPscStatement = testHelper.createCompanyPscStatement();
        pscExemptAsSharesAdmittedOnMarketItem = new PscExemptAsSharesAdmittedOnMarketItem();
        pscExemptAsTradingOnEuRegulatedMarketItem = new PscExemptAsTradingOnEuRegulatedMarketItem();
        pscExemptAsTradingOnRegulatedMarketItem = new PscExemptAsTradingOnRegulatedMarketItem();
        pscExemptAsTradingOnUkRegulatedMarketItem = new PscExemptAsTradingOnUkRegulatedMarketItem();
    }

    @Test
    void statementReturnedByCompanyNumberAndStatementIdFromRepository()
            throws ResourceNotFoundException {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        Optional<PscStatementDocument> pscStatementOptional = Optional.of(document);
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(), anyString())).thenReturn(
                pscStatementOptional);

        Statement statement = pscStatementService.retrievePscStatementFromDb(COMPANY_NUMBER, STATEMENT_ID);

        assertEquals(expectedStatement, statement);
        verify(pscStatementService).retrievePscStatementFromDb(COMPANY_NUMBER, STATEMENT_ID);
        verify(repository).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
    }

    @Test
    void statementListReturnedByCompanyNumberFromRepository() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementList();
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetrics()));
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        verify(repository).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void statementListReturnedByCompanyNumberFromRepositoryWithExemptions()
            throws ResourceNotFoundException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListWithExemptions();
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetrics()));
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(
                Optional.ofNullable(testHelper.createExemptions()));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        verify(repository).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoStatementsExistGetStatementListShouldThrow() {

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(new ArrayList<>()));
        assertThrows(ResourceNotFoundException.class,
                () -> pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25));
    }

    @Test
    void statementListReturnedForCompanyNumberButNoMetricsFound_ShouldReturnList()
            throws ResourceNotFoundException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListNoMetrics();
        document.setData(expectedStatement);

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        verify(repository).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void statementListReturnedByCompanyNumberFromRepositoryRegisterView()
            throws ResourceNotFoundException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListRegisterView();
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetricsRegisterView("public-register")));
        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        verify(repository).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"),
                25);
    }

    @Test
    void statementListThrowsResourceNotFoundExceptionWhenNotPublicRegister()
            throws ResourceNotFoundException {
        // given
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetricsRegisterView("not-public")));

        // when
        Executable actual = () -> pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);

        // then
        assertThrows(ResourceNotFoundException.class, actual);
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        verifyNoInteractions(repository);
    }

    @Test
    void statementListIncWithdrawnCountOneReturnedByCompanyNumberFromRepositoryRegisterView()
            throws ResourceNotFoundException {
        Statement expectedStatement = new Statement();
        expectedStatement.setCeasedOn(LocalDate.parse("2022-12-20"));
        StatementList expectedStatementList = testHelper.createStatementList();
        List<Statement> statement = (List<Statement>) expectedStatementList.getItems();
        statement.get(0).setCeasedOn(LocalDate.parse("2022-12-20"));
        document.setData(expectedStatement);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetricsRegisterView("public-register")));
        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        verify(repository).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"),
                25);
    }

    @Test
    void whenNoMetricsDataFoundForCompanyInRegisterViewShouldThrow() throws ResourceNotFoundException, IOException {
        // given
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        // when
        Exception ex = assertThrows(ResourceNotFoundException.class, () -> {
            pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        });

        // then
        String expectedMessage = "Metrics does not exist for company number: companyNumber";
        String actualMessage = ex.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        verifyNoInteractions(repository);
    }

    @Test
    void whenCompanyNotInPublicRegisterGetStatementListShouldThrow() throws ResourceNotFoundException {
        MetricsApi metricsApi = testHelper.createMetrics();
        RegistersApi registersApi = new RegistersApi();
        metricsApi.setRegisters(registersApi);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(metricsApi));

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> {
            pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        });

        String expectedMessage = "Company companyNumber is not on the public register";
        String actualMessage = ex.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        verifyNoInteractions(repository);
    }

    @Test
    void whenNoStatementsExistInPublicRegisterGetStatementListRegisterViewShouldThrow()
            throws ResourceNotFoundException {
        MetricsApi metricsApi = testHelper.createMetrics();
        RegistersApi registersApi = new RegistersApi();
        RegisterApi api = new RegisterApi();
        api.setMovedOn(OffsetDateTime.parse("2022-12-20T06:00Z"));
        api.setRegisterMovedTo("public-register");
        registersApi.setPersonsWithSignificantControl(api);
        metricsApi.setRegisters(registersApi);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(metricsApi));
        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(
                Optional.of(new ArrayList<>()));

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> {
            pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        });

        String expectedMessage = "No PSC statements exists for company: companyNumber";
        String actualMessage = ex.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        verify(repository).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2022-12-20T06:00Z"),
                25);
    }

    @Test
    void whenNoCountsDataInCompanyMetricsGetStatementListRegisterViewShouldReturnList()
            throws ResourceNotFoundException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListNoMetrics();
        expectedStatementList.setCeasedCount(0);
        document.setData(expectedStatement);
        MetricsApi metricsApi = testHelper.createMetricsRegisterView("public-register");
        metricsApi.setCounts(null);

        when(repository.getStatementListRegisterView(anyString(), anyInt(), any(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(metricsApi));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);

        assertEquals(expectedStatementList, statementList);
        assertNull(statementList.getActiveCount());
        assertNull(statementList.getTotalResults());
        verify(pscStatementService).retrievePscStatementListFromDb(COMPANY_NUMBER, 0, true, 25);
        verify(repository).getStatementListRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"),
                25);
    }

    @Test
    void deletePscStatementDeletesStatement() {
        // Given
        document.setData(testHelper.createStatement());
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(
                Optional.of(document));

        // When
        pscStatementService.deletePscStatement(COMPANY_NUMBER, STATEMENT_ID, DELTA_AT);

        // Then
        verify(repository).delete(document);
        verify(repository).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
        verifyNoMoreInteractions(repository);
        verify(apiClientService).invokeChsKafkaApiDelete(new ResourceChangedRequest(
                COMPANY_NUMBER, STATEMENT_ID, document, true));
    }

    @Test
    void deleteSucceedsWhenStatementIdNotInMongo() {
        // Given
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(), anyString())).thenReturn(
                Optional.empty());

        // When
        pscStatementService.deletePscStatement(COMPANY_NUMBER, NOT_FOUND_STATEMENT_ID, DELTA_AT);

        // Then
        verify(repository).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER,
                NOT_FOUND_STATEMENT_ID);
        verifyNoMoreInteractions(repository);
        verify(apiClientService).invokeChsKafkaApiDelete(new ResourceChangedRequest(COMPANY_NUMBER,
                NOT_FOUND_STATEMENT_ID, new PscStatementDocument(), true));
    }

    @Test
    void deleteSucceedsWhenCompanyNumberNotInMongo() {
        // Given
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(), anyString())).thenReturn(
                Optional.empty());

        // When
        pscStatementService.deletePscStatement(NOT_FOUND_COMPANY_NUMBER, STATEMENT_ID, DELTA_AT);

        // Then
        verify(repository).getPscStatementByCompanyNumberAndStatementId(NOT_FOUND_COMPANY_NUMBER, STATEMENT_ID);
        verifyNoMoreInteractions(repository);
        verify(apiClientService).invokeChsKafkaApiDelete(new ResourceChangedRequest(NOT_FOUND_COMPANY_NUMBER,
                STATEMENT_ID, new PscStatementDocument(), true));
    }

    @Test
    void deleteThrowsBadRequestExceptionWhenNoDeltaAtGiven() {
        // Given

        // When
        Executable actual = () -> pscStatementService.deletePscStatement(COMPANY_NUMBER, STATEMENT_ID,
                null);

        // Then
        assertThrows(BadRequestException.class, actual);
        verifyNoInteractions(repository);
        verifyNoInteractions(apiClientService);
    }

    @Test
    void deleteThrowsConflictExceptionWhenStaleDeltaAtGiven() {
        // Given
        document.setDeltaAt(DELTA_AT);
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(), anyString()))
                .thenReturn(Optional.of(document));

        // When
        Executable actual = () -> pscStatementService.deletePscStatement(COMPANY_NUMBER, STATEMENT_ID,
                "20170101093435661593");

        // Then
        assertThrows(ConflictException.class, actual);
        verify(repository).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(apiClientService);
    }

    @Test
    void deleteThrowsServiceUnavailableExceptionWhenMongoNotAvailableInFind() {
        // Given
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(), anyString()))
                .thenThrow(TransientDataAccessResourceException.class);

        // When
        Executable actual = () -> pscStatementService.deletePscStatement(COMPANY_NUMBER, STATEMENT_ID,
                DELTA_AT);

        // Then
        assertThrows(ServiceUnavailableException.class, actual);
        verify(repository).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(apiClientService);
    }

    @Test
    void deleteThrowsServiceUnavailableExceptionWhenMongoNotAvailableInDelete() {
        // Given
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(), anyString()))
                .thenReturn(Optional.of(document));
        doThrow(TransientDataAccessResourceException.class).when(repository).delete(any());

        // When
        Executable actual = () -> pscStatementService.deletePscStatement(COMPANY_NUMBER, STATEMENT_ID,
                DELTA_AT);

        // Then
        assertThrows(ServiceUnavailableException.class, actual);
        verify(repository).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
        verify(repository).delete(document);
        verifyNoInteractions(apiClientService);
    }

    @Test
    void processPscStatementSavesStatement() {
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(
                document);

        pscStatementService.processPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        verify(repository).save(document);
        verify(repository).findUpdatedPscStatement(eq(COMPANY_NUMBER), eq(STATEMENT_ID), any());
        verify(apiClientService).invokeChsKafkaApi(
                new ResourceChangedRequest(COMPANY_NUMBER, STATEMENT_ID, null, false));
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    void processPscStatementUpdatesStatement() {
        LocalDateTime dateTime = LocalDateTime.now();
        Created created = new Created();
        created.setAt(dateTime);
        document.setCreated(created);
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(
                Optional.of(document));
        ApiResponse<Void> response = new ApiResponse<>(200, null);
        when(apiClientService.invokeChsKafkaApi(any())).thenReturn(response);
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(
                document);

        pscStatementService.processPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        verify(repository).save(document);
        verify(repository).findUpdatedPscStatement(eq(COMPANY_NUMBER), eq(STATEMENT_ID), any());
        verify(apiClientService).invokeChsKafkaApi(new ResourceChangedRequest(COMPANY_NUMBER, STATEMENT_ID,
                null, false));
        assertEquals(document.getCreated().getAt(), dateTime);
    }

    @Test
    void processPscStatementSavesToDbWhenResourceChangedCallFails() {
        LocalDateTime dateTime = LocalDateTime.now();
        Created created = new Created();
        created.setAt(dateTime);
        document.setCreated(created);

        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(
                Optional.of(document));
        when(apiClientService.invokeChsKafkaApi(any())).thenThrow(
                ServiceUnavailableException.class);
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(
                document);

        Executable executable = () -> pscStatementService.processPscStatement(COMPANY_NUMBER, STATEMENT_ID,
                companyPscStatement);

        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).save(document);
        verify(repository).findUpdatedPscStatement(eq(COMPANY_NUMBER), eq(STATEMENT_ID), any());
        verify(apiClientService).invokeChsKafkaApi(
                new ResourceChangedRequest(COMPANY_NUMBER, STATEMENT_ID, null, false));
        assertEquals(document.getCreated().getAt(), dateTime);
    }

    @Test
    void processPscStatementThrowsConflictErrorWhenDeltaAtInPast() {
        // given
        LocalDateTime dateTime = LocalDateTime.now();
        Updated updated = new Updated();
        updated.setAt(dateTime);
        document.setUpdated(updated);
        when(repository.findUpdatedPscStatement(COMPANY_NUMBER, STATEMENT_ID, DELTA_AT)).thenReturn(
                Optional.ofNullable(document));

        // when
        Executable actual = () -> pscStatementService.processPscStatement(COMPANY_NUMBER, STATEMENT_ID,
                companyPscStatement);

        // then
        assertThrows(ConflictException.class, actual);
        verify(repository).findUpdatedPscStatement(eq(COMPANY_NUMBER), eq(STATEMENT_ID), any());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(apiClientService);
        assertEquals(document.getUpdated().getAt(), dateTime);
    }

    @Test
    void hasPscExemptionsReturnsTrueWhenSharesAdmittedOnMarket() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsSharesAdmittedOnMarket(pscExemptAsSharesAdmittedOnMarketItem);
        companyExemptions.setExemptions(testHelper.getAdmittedExemptions());
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(optionalExempt);

        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0,
                false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");
        linksType.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void hasPscExemptionsReturnsTrueWhenTradingOnEuRegulatedMarket() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnEuRegulatedMarket(pscExemptAsTradingOnEuRegulatedMarketItem);
        companyExemptions.setExemptions(testHelper.getEuExemptions());
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(optionalExempt);

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
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnRegulatedMarket(pscExemptAsTradingOnRegulatedMarketItem);
        companyExemptions.setExemptions(exemptions);
        when(companyExemptionsApiService.getCompanyExemptions(COMPANY_NUMBER)).thenReturn(
                Optional.ofNullable(testHelper.createExemptions()));

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
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(pscExemptAsTradingOnUkRegulatedMarketItem);
        companyExemptions.setExemptions(testHelper.getUkExemptions());
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(optionalExempt);

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
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        companyExemptions.setExemptions(exemptions);
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExemptions(COMPANY_NUMBER)).thenReturn(optionalExempt);

        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void hasPscExemptionsReturnsTrueAndExemptTo() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnRegulatedMarket(pscExemptAsTradingOnRegulatedMarketItem);
        companyExemptions.setExemptions(testHelper.getExemptionsWithExemptTo());
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(optionalExempt);

        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");
        linksType.setExemptions(null);

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void hasPscExemptionsReturnsTrueMultipleExemptions() {
        Statement expectedStatement = new Statement();
        document.setData(expectedStatement);
        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(
                Optional.of(Collections.singletonList(document)));

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(pscExemptAsTradingOnUkRegulatedMarketItem);
        companyExemptions.setExemptions(testHelper.getMultipleExemptions());
        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(optionalExempt);

        StatementList list = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER, 0, false, 25);
        StatementLinksType linksType = new StatementLinksType();
        linksType.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control-statements");
        linksType.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(list.getLinks(), linksType);
    }

    @Test
    void processPscStatementCreatesIfDeltaAtIsMissing() {
        when(repository.findUpdatedPscStatement(COMPANY_NUMBER, STATEMENT_ID, DELTA_AT)).thenReturn(Optional.empty());
        ApiResponse<Void> response = new ApiResponse<>(200, null);
        when(apiClientService.invokeChsKafkaApi(any())).thenReturn(response);
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(
                document);

        pscStatementService.processPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);
        verify(repository).save(document);
        verify(repository).findUpdatedPscStatement(eq(COMPANY_NUMBER), eq(STATEMENT_ID), any());
    }

    @Test
    void processPscStatementIfDocumentHasNoDeltaAt() {
        // given
        companyPscStatement.setDeltaAt(null);
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(
                document);

        // when
        pscStatementService.processPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        // then
        verify(repository).save(document);
        verify(repository).findById(STATEMENT_ID);
        verify(apiClientService).invokeChsKafkaApi(new ResourceChangedRequest(
                COMPANY_NUMBER, STATEMENT_ID, null, false));
    }

    @Test
    void processPscStatementSaveToDbIllegalArgumentException() {
        // given
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(
                document);
        when(repository.save(any())).thenThrow(IllegalArgumentException.class);

        // when
        Executable actual = () -> pscStatementService.processPscStatement(COMPANY_NUMBER,
                STATEMENT_ID, companyPscStatement);

        // then
        assertThrows(BadRequestException.class, actual);
        verify(repository).save(document);
        verifyNoInteractions(apiClientService);
    }

    @Test
    void processPscStatementDoesNotUpdateIfDeltaAtIsMissing() {
        // given
        when(repository.findUpdatedPscStatement(COMPANY_NUMBER, STATEMENT_ID, DELTA_AT)).thenReturn(
                Optional.ofNullable(document));

        // when
        Executable actual = () -> pscStatementService.processPscStatement(COMPANY_NUMBER, STATEMENT_ID,
                companyPscStatement);

        // then
        assertThrows(ConflictException.class, actual);
        verify(repository).findUpdatedPscStatement(eq(COMPANY_NUMBER), eq(STATEMENT_ID), any());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(apiClientService);
    }
}