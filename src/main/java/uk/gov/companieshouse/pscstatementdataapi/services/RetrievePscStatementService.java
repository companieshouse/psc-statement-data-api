package uk.gov.companieshouse.pscstatementdataapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDao;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;

public class RetrievePscStatementService {


    @Autowired
    PscStatementRepository pscStatementRepository;

    public PscStatementDao retrievePscStatementFromDb(String companyNumber, String statementId){
        pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
        this.transformPscStatement();
        return null;
    }


    public PscStatementDao transformPscStatement(){

        return null;
    }
}
