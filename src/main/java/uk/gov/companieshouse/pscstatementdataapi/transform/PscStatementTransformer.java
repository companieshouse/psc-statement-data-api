package uk.gov.companieshouse.pscstatementdataapi.transform;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.model.Created;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.Updated;

@Component
public class PscStatementTransformer {

    public PscStatementDocument transformPscStatement(String companyNumber, String statementId,
            CompanyPscStatement companyPscStatement, Created created) {

        LocalDateTime timeNow = LocalDateTime.now();
        PscStatementDocument document = new PscStatementDocument();
        document.setId(statementId);
        document.setCompanyNumber(companyNumber);
        document.setUpdated(new Updated()
                .setAt(timeNow)
                .setBy(DataMapHolder.getRequestId()));
        document.setPscStatementIdRaw(companyPscStatement.getPscStatementIdRaw());
        document.setData(companyPscStatement.getStatement());
        document.setDeltaAt(companyPscStatement.getDeltaAt());
        document.setCreated(created != null ? created : new Created()
                .setAt(timeNow)
                .setBy(DataMapHolder.getRequestId()));

        return document;
    }

}
