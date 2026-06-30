package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.mongodb.BasicDBObject;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.psc.Statement;

@WritingConverter
public class PscStatementWriteConverter implements Converter<Statement, BasicDBObject> {

    private final JsonMapper objectMapper;

    public PscStatementWriteConverter(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BasicDBObject convert(@NonNull Statement source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (JacksonException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
