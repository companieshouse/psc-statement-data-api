package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.converter.ReadConverter;
import uk.gov.companieshouse.api.psc.Statement;

@ReadingConverter
public class PscStatementReadConverter extends ReadConverter<Statement> {
    public PscStatementReadConverter(ObjectMapper objectMapper, Class<Statement> objectClass) {
        super(objectMapper, objectClass);
    }
}
