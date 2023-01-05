package uk.gov.companieshouse.pscstatementdataapi.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PscStatementControllerTest {

    @Autowired
    PscStatementController pscStatementController;
    @Test
    public void contextLoads(){
        assertThat(pscStatementController).isNotNull();
    }



}
