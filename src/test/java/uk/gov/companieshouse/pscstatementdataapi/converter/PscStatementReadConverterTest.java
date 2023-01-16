package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.companieshouse.api.psc.Statement;

import static org.junit.Assert.assertEquals;

public class PscStatementReadConverterTest {

    private String etag = "test123";

    private PscStatementReadConverter pscStatementReadConverter;

    @BeforeEach
    public void setUp(){
        pscStatementReadConverter = new PscStatementReadConverter(new ObjectMapper());
    }
    @Test
    public void correctlyConvertsDocumentToStatementObject(){
        Document source = Document.parse("{\"etag\" : \"test123\"}");
        Statement actualStatement = pscStatementReadConverter.convert(source);
        assertEquals(actualStatement.getEtag(),etag);
    }
}
