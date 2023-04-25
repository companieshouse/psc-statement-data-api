package uk.gov.companieshouse.pscstatementdataapi.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.PscStatementApiService;

/**
 * Loads the application context.
 * Best place to mock your downstream calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest extends AbstractMongoConfig {

    @MockBean
    public PscStatementApiService pscStatementApiService;

    @MockBean
    public CompanyMetricsApiService companyMetricsApiService;

    @MockBean
    public CompanyExemptionsApiService companyExemptionsApiService;

    @MockBean
    public InternalApiClient internalApiClient;
}
