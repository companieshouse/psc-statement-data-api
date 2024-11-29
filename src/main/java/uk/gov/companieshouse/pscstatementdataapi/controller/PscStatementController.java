package uk.gov.companieshouse.pscstatementdataapi.controller;

import static uk.gov.companieshouse.pscstatementdataapi.PSCStatementDataApiApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.services.PscStatementService;

@RestController
@RequestMapping("/company/{company_number}/persons-with-significant-control-statements")
public class PscStatementController {

    private static final Logger logger = LoggerFactory.getLogger(NAMESPACE);

    private final PscStatementService pscStatementService;

    public PscStatementController(PscStatementService pscStatementService) {
        this.pscStatementService = pscStatementService;
    }

    @GetMapping("/{statement_id}")
    public ResponseEntity<Statement> searchPscStatements(@PathVariable("company_number") String companyNumber,
            @PathVariable("statement_id") String statementId)
            throws JsonProcessingException, ResourceNotFoundException {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .statementId(statementId);
        logger.info(String.format("Retrieving psc statement data for company number %s and statement_id %s",
                companyNumber, statementId), DataMapHolder.getLogMap());
        Statement statement = pscStatementService.retrievePscStatementFromDb(companyNumber, statementId);
        return new ResponseEntity<>(statement, HttpStatus.OK);
    }

    @PutMapping("/{statement_id}/internal")
    public ResponseEntity<Void> processPcsStatement(@RequestHeader("x-request-id") String contextId,
            @PathVariable("company_number") String companyNumber,
            @PathVariable("statement_id") String statementId,
            @RequestBody CompanyPscStatement companyPscStatement
    ) throws JsonProcessingException {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .statementId(statementId);
        logger.infoContext(contextId,
                String.format("Processing psc statement data for company number %s and statement_id %s",
                        companyNumber, statementId), DataMapHolder.getLogMap());
        pscStatementService.processPscStatement(contextId, companyNumber, statementId, companyPscStatement);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("")
    public ResponseEntity<StatementList> searchPscStatementsList(
            @PathVariable String companyNumber,
            @RequestParam(value = "items_per_page", required = false, defaultValue = "25") Integer itemsPerPage,
            @RequestParam(value = "start_index", required = false, defaultValue = "0") final Integer startIndex,
            @RequestParam(value = "register_view", required = false) Boolean registerView) {
        itemsPerPage = Math.min(itemsPerPage, 100);
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemsPerPage(itemsPerPage.toString())
                .startIndex(startIndex.toString())
                .registerView(String.valueOf(registerView));
        logger.info(String.format(
                "Retrieving psc statement list data for company number %s, start index %d, items per page %d",
                companyNumber, startIndex, itemsPerPage), DataMapHolder.getLogMap());
        StatementList statementList = pscStatementService.retrievePscStatementListFromDb(companyNumber,
                startIndex,
                registerView,
                itemsPerPage);
        return new ResponseEntity<>(statementList, HttpStatus.OK);
    }

    /**
     * Delete psc-statement information for a statement id.
     *
     * @param statementId the statement id to be deleted
     * @return return 200 status with empty body
     */
    @DeleteMapping("/{statement_id}/internal")
    public ResponseEntity<Void> deletePscStatement(
            @RequestHeader("x-request-id") String contextId,
            @RequestHeader("X-DELTA-AT") String deltaAt,
            @PathVariable("company_number") String companyNumber,
            @PathVariable("statement_id") String statementId) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .contextId(contextId)
                .statementId(statementId);
        logger.infoContext(contextId, String.format(
                        "Deleting Psc statement information for statement id %s", statementId),
                DataMapHolder.getLogMap());
        pscStatementService.deletePscStatement(contextId, companyNumber, statementId, deltaAt);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
