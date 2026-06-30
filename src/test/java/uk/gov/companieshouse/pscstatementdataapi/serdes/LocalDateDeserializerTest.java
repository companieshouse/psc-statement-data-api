package uk.gov.companieshouse.pscstatementdataapi.serdes;

import static org.junit.Assert.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;

class LocalDateDeserializerTest {

    private LocalDateDeserializer deserializer;

    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        deserializer = new LocalDateDeserializer();
        mapper = JsonMapper.builder().build();
    }

    @Test
    void dateShouldDeserialize() {

        String jsonTestString = "{\"date\":{\"$date\": \"2023-01-09T00:00:00Z\"}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        Assertions.assertEquals(LocalDate.of(2023, 1, 9), returnedDate);

    }

    @Test
    void longStringReturnsLong() {

        String jsonTestString = "{\"date\":{\"$date\": {\"$numberLong\":\"-1431388800000\"}}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        Assertions.assertEquals(LocalDate.of(1924, 8, 23), returnedDate);

    }

//    @Test
//    void nullStringReturnsError() {
//        assertThrows(NullPointerException.class, ()->{
//            deserialize(null);
//        });
//    }

    @Test
    void invalidStringReturnsError() {

        String jsonTestString = "{\"date\":{\"$date\": \"NotADate\"}}}";

        assertThrows(BadRequestException.class, ()->{
            deserialize(jsonTestString);
        });
    }

    private LocalDate deserialize(String jsonString) {
        JsonParser parser = mapper.createParser(jsonString);
        DeserializationContext deserializationContext = mapper._deserializationContext();

        parser.nextToken();
        parser.nextToken();
        parser.nextToken();

        return deserializer.deserialize(parser, deserializationContext);
    }

}
