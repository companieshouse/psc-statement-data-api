package uk.gov.companieshouse.pscstatementdataapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementList;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
    @Spy
    DateTransformer dateTransformer;

    @Spy
    @InjectMocks
    PscStatementService pscStatementService;

    private TestHelper testHelper;
    private CompanyPscStatement companyPscStatement;
    private PscStatementDocument document;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper();
        document = testHelper.createEmptyPscStatementDocument();
        companyPscStatement = testHelper.createCompanyPscStatement();
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

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(testHelper.createMetrics()));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, 25);
        verify(repository, times(1)).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoStatementsExistGetStatementListShouldThrow() throws JsonProcessingException{

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(new ArrayList<PscStatementDocument>()));
        assertThrows(ResourceNotFoundException.class, ()-> pscStatementService.retrievePscStatementListFromDb( COMPANY_NUMBER, 0, 25));
    }

    @Test
    void statementListReturnedForCompanyNumberButNoMetricsFound_ShouldReturnList() throws ResourceNotFoundException, IOException {
        Statement expectedStatement = new Statement();
        StatementList expectedStatementList = testHelper.createStatementListNoMetrics();
        document.setData(expectedStatement);

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, 25);
        verify(repository, times(1)).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void deletePscStatementDeletesStatement() {
        ApiResponse<Void> response = new ApiResponse<>(200, null);
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        when(apiClientService.invokeChsKafkaApiWithDeleteEvent(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID, document.getData())).thenReturn(response);

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
}
