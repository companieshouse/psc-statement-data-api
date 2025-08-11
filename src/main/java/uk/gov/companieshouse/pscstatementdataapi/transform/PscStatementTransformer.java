package uk.gov.companieshouse.pscstatementdataapi.transform;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.Created;
import uk.gov.companieshouse.api.model.Updated;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;

@Component
public class PscStatementTransformer {

    public PscStatementDocument transformPscStatement(String companyNumber, String statementId,
            CompanyPscStatement companyPscStatement, Created created) {

        LocalDateTime timeNow = LocalDateTime.now();
        PscStatementDocument document = new PscStatementDocument();
        document.setId(statementId);
        document.setCompanyNumber(companyNumber);
        document.setUpdated(new Updated().setAt(timeNow));
        document.setPscStatementIdRaw(companyPscStatement.getPscStatementIdRaw());
        document.setData(companyPscStatement.getStatement());
        document.setDeltaAt(companyPscStatement.getDeltaAt());
        document.setCreated(created != null ? created : new Created().setAt(timeNow));

        return document;
    }

}
