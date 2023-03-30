package uk.gov.companieshouse.pscstatementdataapi.wiremockTests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.pscstatementdataapi.config.WiremockTestConfig;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles({"test"})
@DirtiesContext
@AutoConfigureMockMvc
public class PscStatementListTests {
    private static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    private static String companyNumber = "12345678";

    @BeforeAll
    static void init() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8888));
        wireMockServer.start();
    }

    @AfterAll
    static void stop() {
        wireMockServer.stop();
    }

    @Test
    void testWiremock() {
        System.out.println(wireMockServer.baseUrl());
        assertTrue(wireMockServer.isRunning());
    }

    @Test
    void companyMetricsApi() throws Exception {

        File metricsFile = new ClassPathResource("/json/input/company_metrics1.json").getFile();

        wireMockServer.stubFor(
                WireMock.get(urlPathMatching("/company/12345678/metrics"))
                        .willReturn(okJson("{\"counts\":{\"persons-with-significant-control\":{\"active_statements_count\":1,\"total_count\":2,\"active_pscs_count\":1," +
                                        "\"ceased_pscs_count\":0,\"pscs_count\":1,\"statements_count\":1,\"withdrawn_statements_count\":0},\"appointments\":{\"total_count\":2," +
                                        "\"active_secretaries_count\":1,\"resigned_count\":0,\"active_llp_members_count\":0,\"active_directors_count\":1," +
                                        "\"active_count\":2}},\"registers\":{\"directors\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}," +
                                        "\"usual_residential_address\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}," +
                                        "\"secretaries\":{\"moved_on\":\"2020-11-09T14:52:14.000Z\",\"register_moved_to\":\"public-register\"}," +
                                        "\"members\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}," +
                                        "\"persons_with_significant_control\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}}}")
                                .withStatus(OK.value())
                                .withHeader("Content-Type", "application/json"))
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/company/{company_number}/persons-with-significant-control-statements?register_view=true", companyNumber)
                        .header("ERIC-IDENTITY", "Test-Identity")
                        .header("ERIC-IDENTITY-TYPE", "key"))
                        .andDo(print()).andExpect(status().is5xxServerError());
    }

    @Test
    void testInvoke() {
        List<ServeEvent> serverEvents = WiremockTestConfig.getServeEvents();
        Assertions.assertTrue(serverEvents.isEmpty());
    }
}
