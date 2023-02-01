package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.Statement.KindEnum;

import static org.junit.Assert.assertTrue;

public class PscStatementWriteConverterTest {

    private static final String KIND = "persons-with-significant-control-statement";

    private PscStatementWriteConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new PscStatementWriteConverter(new ObjectMapper());
    }

    @Test
    public void canConvertDocument() {
        Statement statement = new Statement();
        statement.setKind(KindEnum.PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT);

        BasicDBObject object = converter.convert(statement);
        String json = object.toJson();

        assertTrue(json.contains(KIND));
    }
}