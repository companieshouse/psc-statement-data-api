package uk.gov.companieshouse.pscstatementdataapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.StatementList;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;

import static org.apache.commons.lang3.math.NumberUtils.min;

@RestController
@RequestMapping("/company/{company_number}/persons-with-significant-control-statements")
public class PscStatementController {

    @Autowired
    private Logger logger;
    @Autowired
    private PscStatementService pscStatementService;

    @GetMapping("/{statement_id}")
    public ResponseEntity<Statement> searchPscStatements (@PathVariable("company_number") String companyNumber,
                                                          @PathVariable("statement_id") String statementId) throws JsonProcessingException, ResourceNotFoundException {
        logger.info(String.format("Retrieving psc statement data for company number %s and statement_id %s", companyNumber, statementId));
        Statement statement = pscStatementService.retrievePscStatementFromDb(companyNumber, statementId);
        return new ResponseEntity<>(statement, HttpStatus.OK);
    }

    @PutMapping("/{statement_id}/internal")
    public ResponseEntity<Void> processPcsStatement(@RequestHeader("x-request-id") String contextId,
                                                    @PathVariable("company_number") String companyNumber,
                                                    @PathVariable("statement_id") String statementId,
                                                    @RequestBody CompanyPscStatement companyPscStatement) {
        logger.info(String.format("Processing psc statement data for company number %s and statement_id %s", companyNumber, statementId));
        pscStatementService.processPscStatement(contextId, companyNumber, statementId, companyPscStatement);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("")
    public ResponseEntity<StatementList> searchPscStatementsList(
            @PathVariable String company_number,
            @RequestParam(value = "items_per_page", required = false, defaultValue = "25") Integer itemsPerPage,
            @RequestParam(value = "start_index", required = false, defaultValue = "0") final Integer startIndex) throws JsonProcessingException, ResourceNotFoundException {
        itemsPerPage = Math.min(itemsPerPage, 100);
        logger.info(String.format("Retrieving psc statement list data for company number %s, start index %d, items per page %d", company_number,
                startIndex,
                itemsPerPage));
        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(company_number,
                startIndex,
                itemsPerPage);
        return new ResponseEntity<>(statementList, HttpStatus.OK);
    }

    /**
     * Delete psc-statement information for a statement id.
     *
     * @param  statementId  the statement id to be deleted
     * @return return 200 status with empty body
     */
    @DeleteMapping("/{statement_id}/internal")
    public ResponseEntity<Void> deletePscStatement(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("company_number") String companyNumber,
            @PathVariable("statement_id") String statementId) {
        logger.info(String.format(
                "Deleting Psc statement information for statement id %s", statementId));
        pscStatementService.deletePscStatement(contextId, companyNumber, statementId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
