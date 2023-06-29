package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.converter.ReadConverter;
import uk.gov.companieshouse.api.delta.PscStatement;

public class PscStatementReadConverter extends ReadConverter<PscStatement> {
    public PscStatementReadConverter(ObjectMapper objectMapper, Class<PscStatement> objectClass) {
        super(objectMapper, objectClass);
    }
}
