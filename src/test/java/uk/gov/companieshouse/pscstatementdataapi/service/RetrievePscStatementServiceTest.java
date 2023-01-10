package uk.gov.companieshouse.pscstatementdataapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.services.RetrievePscStatementService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class RetrievePscStatementServiceTest {

    @Mock
    PscStatementRepository repository;

    @Spy
    @InjectMocks
    RetrievePscStatementService pscStatementService;

    @Test
    public void statementReturnedByCompanyNumberAndStatementIdFromRepository() throws JsonProcessingException {
        Statement expectedStatement = new Statement();
        PscStatementDao pscStatementDao = new PscStatementDao();
        pscStatementDao.setData(expectedStatement);
        Optional<PscStatementDao> pscStatementOptional = Optional.of(pscStatementDao);
        when(repository.getPscStatementByCompanyNumberAndStatementId(anyString(),anyString())).thenReturn(pscStatementOptional);

        Statement statement = pscStatementService.retrievePscStatementFromDb("123","testId");

        assertEquals(expectedStatement, statement);
        verify(pscStatementService, times(1)).retrievePscStatementFromDb("123", "testId");
        verify(repository, times(1)).getPscStatementByCompanyNumberAndStatementId("123", "testId");
    }

}
