package uk.gov.companieshouse.pscstatementdataapi.converter;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.exception.PscStatementConversionException;

class PscStatementReadConverterTest {

    private PscStatementReadConverter readConverter;

    @BeforeEach
    void setUp(){
        readConverter = new PscStatementReadConverter(
                JsonMapper.builder()
                        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build()
        );
    }

    @Test
    void correctlyConvertsDocumentToStatementObject(){
        Document source = Document.parse("{\"etag\" : \"etag\"}");
        Statement actualStatement = readConverter.convert(source);
        Assertions.assertNotNull(actualStatement);
        Assertions.assertEquals("etag", actualStatement.getEtag());
    }

    @Test
    void throwsPscStatementConversionExceptionWhenJsonProcessingFails() {
        Document invalidSource = Document.parse("{\"invalid\" : \"invalid\"}");

        RuntimeException exception = Assertions.assertThrows(PscStatementConversionException.class, () -> readConverter.convert(invalidSource));

        Assertions.assertNotNull(exception.getCause());
        Assertions.assertInstanceOf(JacksonException.class, exception.getCause());
    }
}
