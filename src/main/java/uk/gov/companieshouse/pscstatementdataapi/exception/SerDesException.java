package uk.gov.companieshouse.pscstatementdataapi.exception;

public class SerDesException extends RuntimeException {
    public SerDesException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
