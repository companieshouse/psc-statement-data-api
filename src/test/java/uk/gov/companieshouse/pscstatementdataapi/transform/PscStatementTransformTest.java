package uk.gov.companieshouse.pscstatementdataapi.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
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
    void shouldTransformCompanyPscStatement() {
        CompanyPscStatement request = testHelper.createCompanyPscStatement();

        PscStatementDocument document = transformer.transformPscStatement(TestHelper.COMPANY_NUMBER,
                TestHelper.PSC_STATEMENT_ID_RAW, request);

        assertEquals(TestHelper.COMPANY_NUMBER, document.getCompanyNumber());
        assertEquals(TestHelper.PSC_STATEMENT_ID_RAW, document.getPscStatementIdRaw());
        assertEquals(TestHelper.DELTA_AT, document.getDeltaAt());
        assertEquals(testHelper.getStatement(), document.getData());
        assertNotNull(document.getData().getEtag());
        assertTrue(LocalDateTime.now().toEpochSecond(ZoneOffset.MIN)
                - document.getUpdated().getAt().toEpochSecond(ZoneOffset.MIN) < 2);
    }

}
