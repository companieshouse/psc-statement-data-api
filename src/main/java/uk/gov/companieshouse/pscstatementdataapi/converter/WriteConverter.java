package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class WriteConverter<S> implements Converter<S, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public WriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    /**
     * Write convertor.
     * @param source object.
     * @return BSON object to be saved as part of Document.
     */
    @Override
    public BasicDBObject convert(S source) {
        try {
          return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
