package uk.gov.companieshouse.pscstatementdataapi.transform;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.Updated;

import java.time.LocalDateTime;

@Component
public class PscStatementTransformer {

    public PscStatementDocument transformPscStatement(String company_number, String statement_id, CompanyPscStatement companyPscStatement) {

        PscStatementDocument document = new PscStatementDocument();

        companyPscStatement.getStatement().setEtag(GenerateEtagUtil.generateEtag());

        document.setId(statement_id);
        document.setCompanyNumber(company_number);
        document.setUpdated(new Updated().setAt(LocalDateTime.now()));
        document.setPscStatementId(statement_id);
        document.setData(companyPscStatement.getStatement());
        document.setDeltaAt(companyPscStatement.getDeltaAt());

        return document;
    }

}
