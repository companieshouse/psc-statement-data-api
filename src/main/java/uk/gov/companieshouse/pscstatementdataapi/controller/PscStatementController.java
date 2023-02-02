package uk.gov.companieshouse.pscstatementdataapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
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
    public ResponseEntity<Statement> searchPscStatements (@PathVariable("company_number") String companyNumber,
                                                          @PathVariable("statement_id") String statementId) throws JsonProcessingException, ResourceNotFoundException {
        logger.info(String.format("Retrieving psc statement data for company number %s and statement_id %s", companyNumber, statementId));
        Statement statement = pscStatementService.retrievePscStatementFromDb(companyNumber, statementId);
        return new ResponseEntity<>(statement, HttpStatus.OK);
    }

    @PutMapping("/company/{company_number}/persons-with-significant-control-statement/{statement_id}/internal")
    public ResponseEntity<Void> processPcsStatement(@RequestHeader("x-request-id") String contextId,
                                                    @PathVariable("company_number") String companyNumber,
                                                    @PathVariable("statement_id") String statementId,
                                                    @RequestBody CompanyPscStatement companyPscStatement) {
        logger.info(String.format("Processing psc statement data for company number %s and statement_id %s", companyNumber, statementId));
        pscStatementService.processPscStatement(contextId, companyNumber, statementId, companyPscStatement);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete psc-statement information for a statement id.
     *
     * @param  statementId  the statement id to be deleted
     * @return return 200 status with empty body
     */
    @DeleteMapping("/company/{company_number}/persons-with-significant-control-statement/{statement_id}/internal")
    public ResponseEntity<Void> deletePscStatement(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("statement_id") String statementId) {
        logger.info(String.format(
                "Deleting Psc statement information for statement id %s", statementId));
        pscStatementService.deletePscStatement(companyNumber, statementId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
