package uk.gov.companieshouse.pscstatementdataapi.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.pscstatementdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;

/**
 * Loads the application context.
 * Best place to mock your downstream calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest extends AbstractMongoConfig {

    @MockitoBean
    public ChsKafkaApiService chsKafkaApiService;

    @MockitoBean
    public CompanyMetricsApiService companyMetricsApiService;

    @MockitoBean
    public CompanyExemptionsApiService companyExemptionsApiService;

    @MockitoBean
    public InternalApiClient internalApiClient;
}
