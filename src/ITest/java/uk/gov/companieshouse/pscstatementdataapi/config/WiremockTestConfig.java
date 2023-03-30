package uk.gov.companieshouse.pscstatementdataapi.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


public class WiremockTestConfig {

    private static final int port = 8888;

    private static WireMockServer wireMockServer = null;

    public static void setupWiremock() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(port);
            wireMockServer.start();
            configureFor("localhost", port);
        } else {
            wireMockServer.resetAll();
        }
    }

    public static void companyMetricsApi(Integer responseCode, String companyNumber) throws IOException {

        File metricsFile = new ClassPathResource("/json/input/company_metrics1.json").getFile();

        stubFor(
                get(urlPathMatching("/company/OC421554/metrics"))
                        .willReturn(okJson("{\"counts\":{\"persons-with-significant-control\":{\"active_statements_count\":1,\"total_count\":2,\"active_pscs_count\":1," +
                                "\"ceased_pscs_count\":0,\"pscs_count\":1,\"statements_count\":1,\"withdrawn_statements_count\":0},\"appointments\":{\"total_count\":2," +
                                "\"active_secretaries_count\":1,\"resigned_count\":0,\"active_llp_members_count\":0,\"active_directors_count\":1," +
                                "\"active_count\":2}},\"registers\":{\"directors\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}," +
                                "\"usual_residential_address\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}," +
                                "\"secretaries\":{\"moved_on\":\"2020-11-09T14:52:14.000Z\",\"register_moved_to\":\"public-register\"}," +
                                "\"members\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}," +
                                "\"persons_with_significant_control\":{\"register_moved_to\":\"public-register\",\"moved_on\":\"2020-11-09T14:52:14.000Z\"}}}")
                                .withStatus(responseCode)
                                .withHeader("Content-Type", "application/json"))
        );
    }

    public static List<ServeEvent> getServeEvents() {
        return wireMockServer != null ? wireMockServer.getAllServeEvents() : new ArrayList<>();
    }
}


