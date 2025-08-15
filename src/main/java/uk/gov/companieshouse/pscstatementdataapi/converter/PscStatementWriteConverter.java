package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.psc.Statement;

@WritingConverter
public class PscStatementWriteConverter implements Converter<Statement, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public PscStatementWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BasicDBObject convert(Statement source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
