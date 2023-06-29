package uk.gov.companieshouse.pscstatementdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.converter.WriteConverter;
import uk.gov.companieshouse.api.delta.PscStatement;

public class PscStatementWriteConverter extends WriteConverter<PscStatement> {
    public PscStatementWriteConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
