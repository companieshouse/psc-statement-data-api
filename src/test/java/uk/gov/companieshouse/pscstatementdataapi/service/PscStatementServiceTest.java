package uk.gov.companieshouse.pscstatementdataapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PscStatementServiceTest {

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
        StatementList expectedStatementList = new StatementList();
        expectedStatementList.setItems(Collections.singletonList(expectedStatement));
        expectedStatementList.setActiveCount(1);
        expectedStatementList.setCeasedCount(1);
        expectedStatementList.setTotalResults(2);
        expectedStatementList.setStartIndex(0);
        expectedStatementList.setItemsPerPage(25);
        document.setData(expectedStatement);

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.ofNullable(createMetrics()));

        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(COMPANY_NUMBER,0, 25);

        assertEquals(expectedStatementList, statementList);
        verify(pscStatementService, times(1)).retrievePscStatementListFromDb(COMPANY_NUMBER,0, 25);
        verify(repository, times(1)).getStatementList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoStatementsExistGetStatementListShouldThrow() throws JsonProcessingException{

        when(repository.getStatementList(anyString(), anyInt(), anyInt())).thenReturn(Optional.empty());
            try {
               pscStatementService.retrievePscStatementListFromDb( COMPANY_NUMBER, 0, 25);
            }
            catch (ResponseStatusException statusException)  {
                Assertions.assertEquals(HttpStatus.NOT_FOUND, statusException.getStatus());
        }
    }

    private MetricsApi createMetrics() {
        MetricsApi metrics = new MetricsApi();
        CountsApi counts = new CountsApi();
        PscApi pscs = new PscApi();
        pscs.setActiveStatementsCount(1);
        pscs.setWithdrawnStatementsCount(1);
        pscs.setTotalCount(2);
        counts.setPersonsWithSignificantControl(pscs);
        metrics.setCounts(counts);
        return metrics;
    }

    @Test
    void deletePscStatementDeletesStatement() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        pscStatementService.deletePscStatement(COMPANY_NUMBER, STATEMENT_ID);
        verify(repository).delete(document);
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
    }
    @Test
    void deleteThrowsExceptionWhenInvalidIdGiven() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        assertThrows(ResourceNotFoundException.class, () -> pscStatementService.deletePscStatement(COMPANY_NUMBER, "invalid_id"));
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, "invalid_id");
    }
    @Test
    void deleteThrowsExceptionWhenInvalidCompanyNumberGiven() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        assertThrows(ResourceNotFoundException.class, () -> pscStatementService.deletePscStatement("invalid_company_no", STATEMENT_ID));
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
        when(statementTransformer.transformPscStatement(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement)).thenReturn(document);

        pscStatementService.processPscStatement("", COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        verify(repository, times(0)).save(document);
        verify(repository, times(1)).findUpdatedPscStatement(eq(COMPANY_NUMBER),eq(STATEMENT_ID), any());
        assertEquals(document.getUpdated().getAt(), dateTime);
    }
}
