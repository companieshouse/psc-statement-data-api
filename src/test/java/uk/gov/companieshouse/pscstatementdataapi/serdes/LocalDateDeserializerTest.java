package uk.gov.companieshouse.pscstatementdataapi.serdes;

import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;

class LocalDateDeserializerTest {

    private LocalDateDeserializer deserializer;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        deserializer = new LocalDateDeserializer();

        mapper = new ObjectMapper();
    }

    @Test
    void dateShouldDeserialize() throws JsonParseException, IOException{

        String jsonTestString = "{\"date\":{\"$date\": \"2023-01-09T00:00:00Z\"}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        Assertions.assertEquals(LocalDate.of(2023, 1, 9), returnedDate);

    }

    @Test
    void longStringReturnsLong() throws JsonParseException, IOException{

        String jsonTestString = "{\"date\":{\"$date\": {\"$numberLong\":\"-1431388800000\"}}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        Assertions.assertEquals(LocalDate.of(1924, 8, 23), returnedDate);

    }

    @Test
    void nullStringReturnsError() throws JsonParseException, IOException{

        String jsonTestString = null;

        assertThrows(NullPointerException.class, ()->{
            deserialize(jsonTestString);
        });
    }

    @Test
    void invalidStringReturnsError() throws JsonParseException, IOException{

        String jsonTestString = "{\"date\":{\"$date\": \"NotADate\"}}}";

        assertThrows(BadRequestException.class, ()->{
            deserialize(jsonTestString);
        });
    }

    private LocalDate deserialize(String jsonString) throws JsonParseException, IOException {
        JsonParser parser = mapper.getFactory().createParser(jsonString);
        DeserializationContext deserializationContext = mapper.getDeserializationContext();

        parser.nextToken();
        parser.nextToken();
        parser.nextToken();

        return deserializer.deserialize(parser, deserializationContext);
    }
    
}
