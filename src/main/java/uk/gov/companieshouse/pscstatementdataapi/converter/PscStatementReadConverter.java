package uk.gov.companieshouse.pscstatementdataapi.converter;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.exception.PscStatementConversionException;

@ReadingConverter
public class PscStatementReadConverter implements Converter<Document, Statement> {

    private final JsonMapper objectMapper;

    public PscStatementReadConverter(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Statement convert(Document source) {
        try {
            return this.objectMapper.readValue(source.toJson(), Statement.class);
        } catch (JacksonException e) {
            throw new PscStatementConversionException("Failed to convert Json into a statement", e);
        }
    }
}
