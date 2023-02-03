package uk.gov.companieshouse.pscstatementdataapi.utils;

import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.Updated;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

public class TestHelper {

    public static final String PSC_STATEMENT_ID = "statementId";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String DELTA_AT = "20180101093435661593";
    public static final String ETAG = "etag";
    public static final Statement.KindEnum KIND = Statement.KindEnum.PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT;
    public static final Statement.StatementEnum STATEMENT = Statement.StatementEnum.NO_INDIVIDUAL_OR_ENTITY_WITH_SIGNFICANT_CONTROL;

    private Statement statement;
    private CompanyPscStatement companyPscStatement;
    private PscStatementDocument pscStatementDocument;

    public Statement getStatement(){
        return statement;
    }
    public CompanyPscStatement getCompanyPscStatement(){
        return companyPscStatement;
    }
    public PscStatementDocument pscStatementDocument(){
        return pscStatementDocument;
    }

    public Statement createStatement(){
        statement = new Statement();
        statement.setEtag(ETAG);
        statement.setNotifiedOn(LocalDate.now());
        statement.setKind(KIND);
        statement.setStatement(STATEMENT);
        return statement;
    }

    public CompanyPscStatement createCompanyPscStatement(){
        companyPscStatement = new CompanyPscStatement();
        statement = this.createStatement();
        companyPscStatement.setStatement(statement);
        companyPscStatement.setCompanyNumber(COMPANY_NUMBER);
        companyPscStatement.setPscStatementId(PSC_STATEMENT_ID);
        companyPscStatement.setDeltaAt(DELTA_AT);
        return companyPscStatement;
    }

    public PscStatementDocument createEmptyPscStatementDocument(){
        pscStatementDocument = new PscStatementDocument();
        pscStatementDocument.setUpdated(new Updated().setAt(LocalDateTime.now()));
        return pscStatementDocument;
    }

    public String createJsonCompanyPscStatementPayload() throws IOException{
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("psc-statement-example.json"));

        return FileCopyUtils.copyToString(exampleJsonPayload);
    }

    public StatementList createStatementList() {
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(createStatement()));
        return statementList;
    }

}
