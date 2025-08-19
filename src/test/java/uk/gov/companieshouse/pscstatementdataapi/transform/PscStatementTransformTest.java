package uk.gov.companieshouse.pscstatementdataapi.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.pscstatementdataapi.model.Created;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

class PscStatementTransformTest {

    private PscStatementTransformer transformer;

    private TestHelper testHelper;

    @BeforeEach
    public void setUp() {
        transformer = new PscStatementTransformer();
        testHelper = new TestHelper();
    }

    @Test
    void shouldTransformNewCompanyPscStatement() {
        // given
        CompanyPscStatement request = testHelper.createCompanyPscStatement();

        // when
        PscStatementDocument document = transformer.transformPscStatement(TestHelper.COMPANY_NUMBER,
                TestHelper.PSC_STATEMENT_ID_RAW, request, null);

        // then
        assertEquals(TestHelper.COMPANY_NUMBER, document.getCompanyNumber());
        assertEquals(TestHelper.PSC_STATEMENT_ID_RAW, document.getPscStatementIdRaw());
        assertEquals(TestHelper.DELTA_AT, document.getDeltaAt());
        assertEquals(testHelper.getStatement(), document.getData());
        assertEquals(document.getUpdated().getAt(), document.getCreated().getAt());
        assertNotNull(document.getData().getEtag());
    }

    @Test
    void shouldTransformExistingCompanyPscStatement() {
        // given
        LocalDateTime dateTime = LocalDateTime.now();
        Created created = new Created();
        created.setAt(dateTime);
        CompanyPscStatement request = testHelper.createCompanyPscStatement();

        // when
        PscStatementDocument document = transformer.transformPscStatement(TestHelper.COMPANY_NUMBER,
                TestHelper.PSC_STATEMENT_ID_RAW, request, created);

        // then
        assertEquals(TestHelper.COMPANY_NUMBER, document.getCompanyNumber());
        assertEquals(TestHelper.PSC_STATEMENT_ID_RAW, document.getPscStatementIdRaw());
        assertEquals(TestHelper.DELTA_AT, document.getDeltaAt());
        assertEquals(testHelper.getStatement(), document.getData());
        assertEquals(document.getCreated().getAt(), dateTime);
        assertNotNull(document.getData().getEtag());
    }
}
