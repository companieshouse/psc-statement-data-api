package uk.gov.companieshouse.pscstatementdataapi.serdes;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateSerializer extends ValueSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate value, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        if (value == null) {
            jsonGenerator.writeNull();
        } else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String format = value.atStartOfDay().format(dtf);
            jsonGenerator.writeRawValue("ISODate(\"" + format + "\")");
        }
    }
}
