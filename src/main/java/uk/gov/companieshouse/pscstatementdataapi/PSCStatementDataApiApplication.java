package uk.gov.companieshouse.pscstatementdataapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PSCStatementDataApiApplication {

    public static final String NAMESPACE = "psc-statement-data-api";

    public static void main(String[] args) {
        SpringApplication.run(PSCStatementDataApiApplication.class, args);
    }

}