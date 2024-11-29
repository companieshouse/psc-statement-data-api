package uk.gov.companieshouse.pscstatementdataapi.transform;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.Updated;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;

@Component
public class PscStatementTransformer {

    public PscStatementDocument transformPscStatement(String companyNumber, String statementId, CompanyPscStatement companyPscStatement) {

        PscStatementDocument document = new PscStatementDocument();

        document.setId(statementId);
        document.setCompanyNumber(companyNumber);
        document.setUpdated(new Updated().setAt(LocalDateTime.now()));
        document.setPscStatementId(statementId);
        document.setData(companyPscStatement.getStatement());
        document.setDeltaAt(companyPscStatement.getDeltaAt());

        return document;
    }

}
