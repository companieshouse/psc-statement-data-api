package uk.gov.companieshouse.pscstatementdataapi.utils;

import static uk.gov.companieshouse.api.exemptions.PscExemptAsSharesAdmittedOnMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_SHARES_ADMITTED_ON_MARKET;
import static uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnEuRegulatedMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_TRADING_ON_EU_REGULATED_MARKET;
import static uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnRegulatedMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_TRADING_ON_REGULATED_MARKET;
import static uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem.ExemptionTypeEnum.PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.exemptions.ExemptionItem;
import uk.gov.companieshouse.api.exemptions.Exemptions;
import uk.gov.companieshouse.api.exemptions.PscExemptAsSharesAdmittedOnMarketItem;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnEuRegulatedMarketItem;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnRegulatedMarketItem;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.model.Updated;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;

public class TestHelper {

    public static final String PSC_STATEMENT_ID_RAW = "statementIdRaw";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String DELTA_AT = "20180101093435661593";
    public static final String ETAG = "etag";
    public static final String X_REQUEST_ID = "654321";
    public static final Statement.KindEnum KIND = Statement.KindEnum.PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT;
    public static final Statement.StatementEnum STATEMENT = Statement.StatementEnum.NO_INDIVIDUAL_OR_ENTITY_WITH_SIGNFICANT_CONTROL;

    private static final LocalDate EXEMPTION_DATE = LocalDate.of(2022, 11, 3);

    private Statement statement;
    private CompanyPscStatement companyPscStatement;
    private PscStatementDocument pscStatementDocument;

    public Statement getStatement() {
        return statement;
    }

    public CompanyPscStatement getCompanyPscStatement() {
        return companyPscStatement;
    }

    public PscStatementDocument pscStatementDocument() {
        return pscStatementDocument;
    }

    public Statement createStatement() {
        statement = new Statement();
        statement.setEtag(ETAG);
        statement.setNotifiedOn(LocalDate.now());
        statement.setKind(KIND);
        statement.setStatement(STATEMENT);
        return statement;
    }

    public CompanyPscStatement createCompanyPscStatement() {
        companyPscStatement = new CompanyPscStatement();
        statement = this.createStatement();
        companyPscStatement.setStatement(statement);
        companyPscStatement.setCompanyNumber(COMPANY_NUMBER);
        companyPscStatement.setPscStatementIdRaw(PSC_STATEMENT_ID_RAW);
        companyPscStatement.setDeltaAt(DELTA_AT);
        return companyPscStatement;
    }

    public PscStatementDocument createEmptyPscStatementDocument() {
        pscStatementDocument = new PscStatementDocument();
        pscStatementDocument.setUpdated(new Updated().setAt(LocalDateTime.now()));
        return pscStatementDocument;
    }

    public String createJsonCompanyPscStatementPayload() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("psc-statement-example.json"));

        return FileCopyUtils.copyToString(exampleJsonPayload);
    }

    public StatementList createStatementList() {
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(new Statement()));
        statementList.setActiveCount(1);
        statementList.setCeasedCount(1);
        statementList.setTotalResults(2);
        statementList.setStartIndex(0);
        statementList.setItemsPerPage(25);
        statementList.setLinks(createLinks());
        return statementList;
    }

    public StatementList createStatementListWithExemptions() {
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(new Statement()));
        statementList.setActiveCount(1);
        statementList.setCeasedCount(1);
        statementList.setTotalResults(2);
        statementList.setStartIndex(0);
        statementList.setItemsPerPage(25);
        statementList.setLinks(createLinksWithExemptions());
        return statementList;
    }


    public StatementList createStatementListRegisterView() {
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(new Statement()));
        statementList.setActiveCount(1);
        statementList.setCeasedCount(0);
        statementList.setTotalResults(1);
        statementList.setStartIndex(0);
        statementList.setItemsPerPage(25);
        statementList.setLinks(createLinks());
        return statementList;
    }

    public StatementList createStatementListNoMetrics() {
        StatementList statementList = new StatementList();
        statementList.setItems(Collections.singletonList(new Statement()));
        statementList.setStartIndex(0);
        statementList.setItemsPerPage(25);
        statementList.setLinks(createLinks());
        return statementList;
    }

    private StatementLinksType createLinks() {
        StatementLinksType links = new StatementLinksType();
        links.setSelf(String.format("/company/%s/persons-with-significant-control-statements", COMPANY_NUMBER));
        return links;
    }

    private StatementLinksType createLinksWithExemptions() {
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

    public CompanyExemptions createExemptions() {
        CompanyExemptions exemptions = new CompanyExemptions();
        exemptions.setExemptions(getExemptions());
        return exemptions;
    }


    private Exemptions getExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);

        PscExemptAsTradingOnRegulatedMarketItem nonUkEeaStateMarket = new PscExemptAsTradingOnRegulatedMarketItem();

        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_REGULATED_MARKET);

        PscExemptAsTradingOnUkRegulatedMarketItem ukEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnRegulatedMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(ukEeaStateMarket);

        return exemptions;
    }

    public Exemptions getExemptionsWithExemptTo() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(EXEMPTION_DATE);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);

        PscExemptAsTradingOnRegulatedMarketItem nonUkEeaStateMarket = new PscExemptAsTradingOnRegulatedMarketItem();

        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_REGULATED_MARKET);

        PscExemptAsTradingOnUkRegulatedMarketItem ukEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnRegulatedMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(ukEeaStateMarket);

        return exemptions;
    }

    public Exemptions getMultipleExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);

        ExemptionItem ceasedExemptionItem = new ExemptionItem();
        ceasedExemptionItem.exemptFrom(EXEMPTION_DATE);
        ceasedExemptionItem.exemptTo(EXEMPTION_DATE);

        List<ExemptionItem> exemptionItems = Arrays.asList(exemptionItem, ceasedExemptionItem);

        PscExemptAsTradingOnRegulatedMarketItem nonUkEeaStateMarket = new PscExemptAsTradingOnRegulatedMarketItem();

        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_REGULATED_MARKET);

        PscExemptAsTradingOnUkRegulatedMarketItem ukEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnRegulatedMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(ukEeaStateMarket);

        return exemptions;
    }

    public Exemptions getUkExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);

        PscExemptAsTradingOnUkRegulatedMarketItem nonUkEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();

        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        PscExemptAsTradingOnUkRegulatedMarketItem ukEeaStateMarket = new PscExemptAsTradingOnUkRegulatedMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_UK_REGULATED_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(ukEeaStateMarket);

        return exemptions;
    }

    public Exemptions getAdmittedExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);

        PscExemptAsSharesAdmittedOnMarketItem nonUkEeaStateMarket = new PscExemptAsSharesAdmittedOnMarketItem();

        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_SHARES_ADMITTED_ON_MARKET);

        PscExemptAsSharesAdmittedOnMarketItem ukEeaStateMarket = new PscExemptAsSharesAdmittedOnMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_SHARES_ADMITTED_ON_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsSharesAdmittedOnMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsSharesAdmittedOnMarket(ukEeaStateMarket);

        return exemptions;
    }

    public Exemptions getEuExemptions() {
        ExemptionItem exemptionItem = new ExemptionItem();
        exemptionItem.exemptFrom(EXEMPTION_DATE);
        exemptionItem.exemptTo(null);

        List<ExemptionItem> exemptionItems = Collections.singletonList(exemptionItem);

        PscExemptAsTradingOnEuRegulatedMarketItem nonUkEeaStateMarket = new PscExemptAsTradingOnEuRegulatedMarketItem();

        nonUkEeaStateMarket.setItems(exemptionItems);
        nonUkEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_EU_REGULATED_MARKET);

        PscExemptAsTradingOnEuRegulatedMarketItem ukEeaStateMarket = new PscExemptAsTradingOnEuRegulatedMarketItem();
        ukEeaStateMarket.setItems(exemptionItems);
        ukEeaStateMarket.setExemptionType(PSC_EXEMPT_AS_TRADING_ON_EU_REGULATED_MARKET);

        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnEuRegulatedMarket(nonUkEeaStateMarket);
        exemptions.setPscExemptAsTradingOnEuRegulatedMarket(ukEeaStateMarket);

        return exemptions;
    }

}
