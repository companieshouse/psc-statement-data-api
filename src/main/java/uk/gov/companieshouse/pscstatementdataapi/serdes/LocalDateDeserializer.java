package uk.gov.companieshouse.pscstatementdataapi.serdes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;


public class LocalDateDeserializer extends ValueDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            JsonNode jsonNode = jsonParser.readValueAsTree();
            JsonNode dateNode = jsonNode.get("$date");

            return dateNode.isString() ?
                    LocalDate.parse(dateNode.asString(), dateTimeFormatter) :
                    Instant.ofEpochMilli(dateNode.get("$numberLong").asLong())
                            .atZone(ZoneOffset.UTC).toLocalDate();

        } catch (Exception exception) {
            throw new BadRequestException("Deserialization failed.", exception);
        }
    }

}
