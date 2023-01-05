package uk.gov.companieshouse.pscstatementdataapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.services.RetrievePscStatementService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PscStatementControllerTest {
    
    @MockBean
    private RetrievePscStatementService pscStatementService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    PscStatementController pscStatementController;

    @Test
    public void contextLoads(){
        assertThat(pscStatementController).isNotNull();
    }

    @Test
    public void getStatement() throws Exception {

        when(pscStatementService.retrievePscStatementFromDb("123","xyz")).thenReturn(new Statement());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/psc-statements/123/xyz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-IDENTITY", "Test-Identity")
                        .header("ERIC-IDENTITY-TYPE", "Key"))
                        .andExpect(status().isOk());

    }


}
