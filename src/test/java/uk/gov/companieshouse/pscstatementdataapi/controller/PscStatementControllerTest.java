package uk.gov.companieshouse.pscstatementdataapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PscStatementControllerTest {

    private static final String COMPANY_NUMBER = "123";
    private static final String STATEMENT_ID = "xyz";
    private static final String GET_URL = String.format("/company/%s/persons-with-significant-control-statement/%s", COMPANY_NUMBER, STATEMENT_ID);

    private static final String DELETE_URL = String.format("/company/%s/persons-with-significant-control-statement/%s/internal", COMPANY_NUMBER, STATEMENT_ID);

    @MockBean
    private PscStatementService pscStatementService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    PscStatementController pscStatementController;

    @Test
    public void contextLoads(){
        assertThat(pscStatementController).isNotNull();
    }

    @Test
    public void statementResponseReturnedWhenGetRequestInvoked() throws Exception {

        when(pscStatementService.retrievePscStatementFromDb(COMPANY_NUMBER,STATEMENT_ID)).thenReturn(getStatementObject());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-IDENTITY", "Test-Identity")
                        .header("ERIC-IDENTITY-TYPE", "Key"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.etag").value(STATEMENT_ID));

    }

    @Test
    @DisplayName("PSC-STATEMENT DELETE request")
    public void callDisqualifiedOfficerDeleteRequest() throws Exception {

        doNothing()
                .when(pscStatementService).deletePscStatement(anyString(), anyString());

        mockMvc.perform(delete(DELETE_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key"))
                .andExpect(status().isOk());
    }

    public Statement getStatementObject(){
        Statement statement = new Statement();
        statement.setEtag("xyz");
        statement.setNotifiedOn(LocalDate.now());
        return statement;
    }




}
