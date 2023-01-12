package uk.gov.companieshouse.pscstatementdataapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.time.LocalDate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.pscstatementdataapi.serialization.LocalDateDeserializer;

@SpringBootTest

public class LocalDateDeserializerTest {

    private LocalDateDeserializer deserializer;

    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        deserializer = new LocalDateDeserializer();

        mapper = new ObjectMapper();
    }

    @Test
    public void dateShouldDeserialize() throws JsonParseException, IOException{

        String jsonTestString = "{\"date\":{\"$date\": \"2023-01-09T00:00:00Z\"}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        assertEquals(returnedDate, LocalDate.of(2023, 1, 9));

    }

    @Test
    public void longStringReturnsLong() throws JsonParseException, IOException{

        String jsonTestString = "{\"date\":{\"$date\": {\"$numberLong\":\"-1431388800000\"}}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        assertEquals(returnedDate, LocalDate.of(1924, 8, 23));

    }

    @Test
    public void nullStringReturnsError() throws JsonParseException, IOException{

        String jsonTestString = null;

        assertThrows(NullPointerException.class, ()->{

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
