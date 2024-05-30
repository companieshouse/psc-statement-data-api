package uk.gov.companieshouse.pscstatementdataapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"uk.gov.companieshouse.api.api.*"})
public class PSCStatementDataApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PSCStatementDataApiApplication.class, args);

    }

}