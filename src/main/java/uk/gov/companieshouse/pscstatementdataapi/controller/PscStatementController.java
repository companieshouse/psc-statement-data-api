package uk.gov.companieshouse.pscstatementdataapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.services.RetrievePscStatementService;

@RestController
public class PscStatementController {

    @Autowired
    private Logger logger;
    @Autowired
    private RetrievePscStatementService retrievePscStatementService;

    @GetMapping("/company/{company_number}/persons-with-significant-control-statement/{statement_id}")
    public ResponseEntity<Statement> searchPscStatements(@PathVariable String company_number, @PathVariable String statement_id) throws JsonProcessingException {
        logger.info(String.format("Retrieving psc statement data for company number %s and statement_id %s", company_number, statement_id));
        Statement statement = retrievePscStatementService.retrievePscStatementFromDb(company_number, statement_id);
        return new ResponseEntity<>(statement, HttpStatus.OK);
    }
}
