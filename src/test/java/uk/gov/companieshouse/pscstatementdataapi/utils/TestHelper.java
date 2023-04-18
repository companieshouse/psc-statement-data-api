package uk.gov.companieshouse.pscstatementdataapi.utils;

import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.Updated;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;

public class TestHelper {

    public static final String PSC_STATEMENT_ID = "statementId";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String DELTA_AT = "20180101093435661593";
    public static final String ETAG = "etag";
    public static final String X_REQUEST_ID = "654321";
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
        Statement statement = new Statement();
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(statement));
        statementList.setActiveCount(1);
        statementList.setCeasedCount(1);
        statementList.setTotalResults(2);
        statementList.setStartIndex(0);
        statementList.setItemsPerPage(25);
        statementList.setLinks(createLinks());
        return statementList;
    }

    public StatementList createStatementListRegisterView() {
        Statement statement = new Statement();
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(statement));
        statementList.setActiveCount(1);
        statementList.setCeasedCount(0);
        statementList.setTotalResults(1);
        statementList.setStartIndex(0);
        statementList.setItemsPerPage(25);
        statementList.setLinks(createLinks());
        return statementList;
    }

    public StatementList createStatementListNoMetrics() {
        Statement statement = new Statement();
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(statement));
        statementList.setStartIndex(0);
        statementList.setItemsPerPage(25);
        statementList.setLinks(createLinks());
        return statementList;
    }

    private StatementLinksType createLinks() {
        StatementLinksType links = new StatementLinksType();
        links.setSelf(String.format("/company/%s/persons-with-significant-control-statements", COMPANY_NUMBER));
        links.setExemptions(String.format("/company/%s/exemptions", COMPANY_NUMBER));
        return links;
    }

    public MetricsApi createMetrics() {
        MetricsApi metrics = new MetricsApi();
        CountsApi counts = new CountsApi();
        PscApi pscs = new PscApi();
        pscs.setActiveStatementsCount(1);
        pscs.setWithdrawnStatementsCount(1);
        pscs.setStatementsCount(2);
        counts.setPersonsWithSignificantControl(pscs);
        metrics.setCounts(counts);
        return metrics;
    }

    public MetricsApi createMetricsRegisterView() {
        MetricsApi metrics = new MetricsApi();
        CountsApi counts = new CountsApi();
        PscApi pscs = new PscApi();
        pscs.setActiveStatementsCount(1);
        pscs.setWithdrawnStatementsCount(0);
        pscs.setStatementsCount(1);
        counts.setPersonsWithSignificantControl(pscs);
        metrics.setCounts(counts);

        RegistersApi registers = new RegistersApi();
        RegisterApi pscStatements = new RegisterApi();
        pscStatements.setRegisterMovedTo("public-register");
        String date = "2020-12-20T06:00:00Z";
        OffsetDateTime dt = OffsetDateTime.parse(date);
        pscStatements.setMovedOn(dt);
        registers.setPersonsWithSignificantControl(pscStatements);
        metrics.setRegisters(registers);
        return metrics;
    }

}
