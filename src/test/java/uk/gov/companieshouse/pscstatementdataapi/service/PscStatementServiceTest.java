package uk.gov.companieshouse.pscstatementdataapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PscStatementServiceTest {

    private  static  final String STATEMENT_ID = "statementId";
    private static final String COMPANY_NUMBER = "companyNumber";

    @Mock
    PscStatementRepository repository;

    @Spy
    @InjectMocks
    PscStatementService pscStatementService;

    private PscStatementDocument document;

    @BeforeEach
    public void setUp() {
        document = new PscStatementDocument();
    }
    @Test
    public void statementReturnedByCompanyNumberAndStatementIdFromRepository() throws JsonProcessingException, ResourceNotFoundException {
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
    public void deletePscStatementDeletesStatement() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        pscStatementService.deletePscStatement(COMPANY_NUMBER, STATEMENT_ID);
        verify(repository).delete(document);
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID);
    }
    @Test
    public void deleteThrowsExceptionWhenInvalidIdGiven() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        assertThrows(ResourceNotFoundException.class, () -> pscStatementService.deletePscStatement(COMPANY_NUMBER, "invalid_id"));
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, "invalid_id");
    }
    @Test
    public void deleteThrowsExceptionWhenInvalidCompanyNumberGiven() {
        when(repository.getPscStatementByCompanyNumberAndStatementId(COMPANY_NUMBER, STATEMENT_ID)).thenReturn(Optional.of(document));
        assertThrows(ResourceNotFoundException.class, () -> pscStatementService.deletePscStatement("invalid_company_no", STATEMENT_ID));
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId("invalid_company_no", STATEMENT_ID);
    }

}
