package uk.gov.companieshouse.pscstatementdataapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;

@RestController
public class PscStatementController {

    @Autowired
    private Logger logger;
    @Autowired
    private PscStatementService pscStatementService;

    @GetMapping("/company/{company_number}/persons-with-significant-control-statement/{statement_id}")
    public ResponseEntity<Statement> searchPscStatements(@PathVariable String company_number, @PathVariable String statement_id) throws JsonProcessingException, ResourceNotFoundException {
        logger.info(String.format("Retrieving psc statement data for company number %s and statement_id %s", company_number, statement_id));
        Statement statement = pscStatementService.retrievePscStatementFromDb(company_number, statement_id);
        return new ResponseEntity<>(statement, HttpStatus.OK);
    }
    /**
     * Delete psc-statement information for a statement id.
     *
     * @param  statementId  the statement id to be deleted
     * @return return 200 status with empty body
     */
    @DeleteMapping("/company/{company_number}/persons-with-significant-control-statement/{statement_id}/Internal")
    public ResponseEntity<Void> deletePscStatement(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("statement_id") String statementId) {
        logger.info(String.format(
                "Deleting Psc statement information for statement id %s", statementId));
        pscStatementService.deletePscStatement(companyNumber, statementId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
