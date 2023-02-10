package uk.gov.companieshouse.pscstatementdataapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PscStatementControllerTest {
    private static final String GET_URL = String.format("/company/%s/persons-with-significant-control-statements/%s", TestHelper.COMPANY_NUMBER, TestHelper.PSC_STATEMENT_ID);
    private static final String PUT_URL = String.format("/company/%s/persons-with-significant-control-statements/%s/internal", TestHelper.COMPANY_NUMBER, TestHelper.PSC_STATEMENT_ID);
    private static final String DELETE_URL = String.format("/company/%s/persons-with-significant-control-statements/%s/internal", TestHelper.COMPANY_NUMBER, TestHelper.PSC_STATEMENT_ID);
    private static final String GET_STATEMENT_LIST_URL = String.format("/company/%s/persons-with-significant-control-statements", TestHelper.COMPANY_NUMBER);
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "Key";
    private static final String ERIC_PRIVILEGES = "internal-app";
    private static final String X_REQUEST_ID = "654321";


    @MockBean
    private PscStatementService pscStatementService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PscStatementController pscStatementController;

    private TestHelper testHelper;

    @BeforeEach
    void setUp(){
        testHelper = new TestHelper();
    }

    @Test
    void contextLoads(){
        assertThat(pscStatementController).isNotNull();
    }

    @Test
    void statementResponseReturnedWhenGetRequestInvoked() throws Exception {
        when(pscStatementService.retrievePscStatementFromDb(TestHelper.COMPANY_NUMBER,TestHelper.PSC_STATEMENT_ID))
                .thenReturn(testHelper.createStatement());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-IDENTITY", ERIC_IDENTITY)
                        .header("ERIC-IDENTITY-TYPE", ERIC_IDENTITY_TYPE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.etag").value(TestHelper.ETAG));
    }

    @Test
    void callPscStatementPutRequest() throws Exception {
        doNothing()
                .when(pscStatementService).processPscStatement(anyString(), anyString(), anyString(),
                isA(CompanyPscStatement.class));

        mockMvc.perform(put(PUT_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_PRIVILEGES)
                        .content(testHelper.createJsonCompanyPscStatementPayload()))
                .andExpect(status().isOk());
    }
    @Test
    void callPscStatementListGetRequestWithParams() throws Exception {
        when(pscStatementService.retrievePscStatementListFromDb(TestHelper.COMPANY_NUMBER, 2, 5))
                .thenReturn(testHelper.createStatementList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-IDENTITY", ERIC_IDENTITY)
                        .header("ERIC-IDENTITY-TYPE", ERIC_IDENTITY_TYPE)
                        .header("items_per_page", 5)
                        .header("start_index", 2))
                .andExpect(status().isOk());
    }

    @Test
    void callPscStatementListGetRequestNoParams() throws Exception {
        when(pscStatementService.retrievePscStatementListFromDb(TestHelper.COMPANY_NUMBER, 0, 25))
                .thenReturn(testHelper.createStatementList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_STATEMENT_LIST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-IDENTITY", ERIC_IDENTITY)
                        .header("ERIC-IDENTITY-TYPE", ERIC_IDENTITY_TYPE))
                .andExpect(status().isOk());
    }

    @Test
    void callPscStatementDeleteRequest() throws Exception {

        doNothing()
                .when(pscStatementService).deletePscStatement(anyString(), anyString());

        mockMvc.perform(delete(DELETE_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_PRIVILEGES))
                .andExpect(status().isOk());
    }

}
