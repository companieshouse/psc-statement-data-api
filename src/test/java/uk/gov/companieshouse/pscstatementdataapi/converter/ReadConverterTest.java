package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc.Statement;

class ReadConverterTest {

    private ReadConverter<Statement> readConverter;

    @BeforeEach
    void setUp(){
        readConverter = new ReadConverter<>(new ObjectMapper(), Statement.class);
    }

    @Test
    void correctlyConvertsDocumentToStatementObject(){
        Document source = Document.parse("{\"etag\" : \"etag\"}");
        Statement actualStatement = readConverter.convert(source);
        Assertions.assertNotNull(actualStatement);
        Assertions.assertEquals(actualStatement.getEtag(), "etag");
    }
}
