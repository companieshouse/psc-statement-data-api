package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.converter.WriteConverter;
import uk.gov.companieshouse.api.psc.Statement;

@WritingConverter
public class PscStatementWriteConverter extends WriteConverter<Statement> {

    public PscStatementWriteConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
