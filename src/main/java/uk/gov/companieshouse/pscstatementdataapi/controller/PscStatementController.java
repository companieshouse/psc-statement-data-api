package uk.gov.companieshouse.pscstatementdataapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;
import uk.gov.companieshouse.pscstatementdataapi.services.RetrievePscStatementService;
import uk.gov.companieshouse.logging.Logger;
@RestController
public class PscStatementController {

    @Autowired
    private RetrievePscStatementService retrievePscStatementService;

    @GetMapping("/psc-statements/{company_number}/{statement_id}")
    public ResponseEntity<Statement> searchPscStatements(@PathVariable String company_number, @PathVariable String statement_id) throws JsonProcessingException {

        System.out.println("Retrieving psc statement data for company number" + company_number +
                        "and statementId "+statement_id);

        Statement statement = retrievePscStatementService.retrievePscStatementFromDb(company_number, statement_id);
        return new ResponseEntity<>(statement, HttpStatus.OK);
    }
}
