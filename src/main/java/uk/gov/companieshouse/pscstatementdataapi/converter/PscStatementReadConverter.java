package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.psc.Statement;

@ReadingConverter
public class PscStatementReadConverter implements Converter<Document, Statement> {

    private final ObjectMapper objectMapper;

    public PscStatementReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Statement convert(Document source) {
        try {
            return this.objectMapper.readValue(source.toJson(), Statement.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
