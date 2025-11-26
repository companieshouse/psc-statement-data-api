package uk.gov.companieshouse.pscstatementdataapi.exception;

public class PscStatementConversionException extends RuntimeException {
    public PscStatementConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
