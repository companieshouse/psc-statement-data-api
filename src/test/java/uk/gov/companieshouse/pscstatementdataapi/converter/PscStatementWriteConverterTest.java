package uk.gov.companieshouse.pscstatementdataapi.converter;

import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.Statement.KindEnum;

class PscStatementWriteConverterTest {

    private static final KindEnum KIND = Statement.KindEnum.PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT;

    private PscStatementWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PscStatementWriteConverter(new ObjectMapper());
    }

    @Test
    void canConvertStatementDocument() {
        Statement statement = new Statement();
        statement.setKind(KIND);

        BasicDBObject object = converter.convert(statement);
        Assertions.assertNotNull(object);
        String json = object.toJson();

        Assertions.assertTrue(json.contains(KIND.getValue()));
    }

    @Test
    void assertThrowsJsonException() {
        assertThrows(RuntimeException.class, () -> converter.convert(null));
    }

    @Test
    void throwsRuntimeExceptionWhenJsonProcessingFails() throws JsonProcessingException {
        Statement statement = new Statement();

        ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(mockObjectMapper.writeValueAsString(statement))
                .thenThrow(new JsonProcessingException("Test exception") {});

        PscStatementWriteConverter converter = new PscStatementWriteConverter(mockObjectMapper);
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert(statement));
    }
}
