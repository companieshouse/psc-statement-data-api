package uk.gov.companieshouse.pscstatementdataapi.controller;

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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PscStatementControllerTest {

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

        when(pscStatementService.retrievePscStatementFromDb("123","xyz")).thenReturn(getStatementObject());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/company/123/persons-with-significant-control-statement/xyz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-IDENTITY", "Test-Identity")
                        .header("ERIC-IDENTITY-TYPE", "Key"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.etag").value("xyz"));

    }

    public Statement getStatementObject(){
        Statement statement = new Statement();
        statement.setEtag("xyz");
        statement.setNotifiedOn(LocalDate.now());
        return statement;
    }




}
