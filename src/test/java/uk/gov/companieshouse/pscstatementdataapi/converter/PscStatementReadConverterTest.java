package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc.Statement;

class PscStatementReadConverterTest {

    private PscStatementReadConverter readConverter;

    @BeforeEach
    void setUp(){
        readConverter = new PscStatementReadConverter(new ObjectMapper());
    }

    @Test
    void correctlyConvertsDocumentToStatementObject(){
        Document source = Document.parse("{\"etag\" : \"etag\"}");
        Statement actualStatement = readConverter.convert(source);
        Assertions.assertNotNull(actualStatement);
        Assertions.assertEquals("etag", actualStatement.getEtag());
    }

    @Test
    void throwsRuntimeExceptionWhenJsonProcessingFails() {
        Document invalidSource = Document.parse("{\"invalid\" : \"invalid\"}");

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            readConverter.convert(invalidSource);
        });

        Assertions.assertNotNull(exception.getCause());
        Assertions.assertInstanceOf(JsonProcessingException.class, exception.getCause());
    }
}
