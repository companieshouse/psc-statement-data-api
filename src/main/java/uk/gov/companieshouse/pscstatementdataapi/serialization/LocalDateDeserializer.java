package uk.gov.companieshouse.pscstatementdataapi.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            JsonNode jsonNode = jsonParser.readValueAsTree();
            JsonNode dateNode = jsonNode.get("$date");

            return dateNode.textValue() != null ?
                    LocalDate.parse(dateNode.textValue(), dateTimeFormatter) :
                    LocalDate.ofInstant(Instant.ofEpochMilli(dateNode.get("$numberLong").asLong()), ZoneId.systemDefault());
        } catch (Exception exception) {
            throw new BadRequestException("Deserialization failed.", exception);
        }
    }

}
