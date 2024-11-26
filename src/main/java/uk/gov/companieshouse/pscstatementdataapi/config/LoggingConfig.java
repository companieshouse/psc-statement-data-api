package uk.gov.companieshouse.pscstatementdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class LoggingConfig {

    @Value("${logger.namespace}")
    private String loggerNamespace;

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(loggerNamespace);
    }
}
