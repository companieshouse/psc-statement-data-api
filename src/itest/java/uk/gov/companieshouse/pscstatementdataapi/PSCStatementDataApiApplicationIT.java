package uk.gov.companieshouse.pscstatementdataapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.pscstatementdataapi.config.AbstractMongoConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PSCStatementDataApiApplicationIT extends AbstractMongoConfig {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldStartApplication() {
        Executable executable = () -> PSCStatementDataApiApplication.main(new String[0]);
        assertDoesNotThrow(executable);
    }

    @Test
    void healthEndpointShouldBeAvailableAndUp() {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/healthcheck", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

}
