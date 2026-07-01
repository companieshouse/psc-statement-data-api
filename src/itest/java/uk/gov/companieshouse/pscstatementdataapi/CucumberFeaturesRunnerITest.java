package uk.gov.companieshouse.pscstatementdataapi;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.SelectFiles;
import org.junit.platform.suite.api.Suite;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.pscstatementdataapi.config.AbstractIntegrationTest;

@Suite
@IncludeEngines("cucumber")
@TestPropertySource(properties = {"mongodb.transactional = true"})
@CucumberContextConfiguration
@SelectFiles(
        value = {
                @SelectFile("src/itest/resources/features/psc_statements.feature"),
                @SelectFile("src/itest/resources/features/psc_statements_delete.feature"),
                @SelectFile("src/itest/resources/features/psc_statements_response_codes.feature"),
        }
)
public class CucumberFeaturesRunnerITest extends AbstractIntegrationTest {

}
