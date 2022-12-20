package uk.gov.companieshouse.pscstatementdataapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;
import uk.gov.companieshouse.pscstatementdataapi.services.RetrievePscStatementService;

public class PscStatementController {

    @Autowired
    RetrievePscStatementService retrievePscStatementService;

    @GetMapping("/psc-statements/{company_number}/{statement_id}")
    public ResponseEntity<PscStatementDao> searchPscStatements(@PathVariable String company_number, @PathVariable String statement_id){
    retrievePscStatementService.retrievePscStatementFromDb(company_number, statement_id);
    return new ResponseEntity<>();
    }
}
