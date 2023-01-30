package uk.gov.companieshouse.pscstatementdataapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PscStatementControllerTest {

    @MockBean
    private PscStatementService pscStatementService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PscStatementController pscStatementController;

    private TestHelper testHelper;

    public  static  final String ERIC_IDENTITY = "Test-Identity";
    public  static  final String ERIC_IDENTITY_TYPE = "Key";
    public  static  final String X_REQUEST_ID = "654321";

    @BeforeEach
    public void setUp(){
        testHelper = new TestHelper();
    }

    @Test
    public void contextLoads(){
        assertThat(pscStatementController).isNotNull();
    }

    @Test
    public void statementResponseReturnedWhenGetRequestInvoked() throws Exception {
        when(pscStatementService.retrievePscStatementFromDb(TestHelper.COMPANY_NUMBER,TestHelper.PSC_STATEMENT_ID))
                .thenReturn(testHelper.createStatement());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format("/company/%s/persons-with-significant-control-statement/%s",
                                TestHelper.COMPANY_NUMBER, TestHelper.PSC_STATEMENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-IDENTITY", ERIC_IDENTITY)
                        .header("ERIC-IDENTITY-TYPE", ERIC_IDENTITY_TYPE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.etag").value(TestHelper.ETAG));
    }

    @Test
    public void callPscStatementPutRequest() throws Exception {
        doNothing()
                .when(pscStatementService).processPscStatement(anyString(), anyString(), anyString(),
                isA(CompanyPscStatement.class));

        mockMvc.perform(put(String.format("/company/%s/persons-with-significant-control-statement/%s/internal",
                        TestHelper.COMPANY_NUMBER, TestHelper.PSC_STATEMENT_ID))
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .content(testHelper.createJsonCompanyPscStatementPayload()))
                .andExpect(status().isOk());
    }

}
