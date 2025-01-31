package uk.gov.companieshouse.pscstatementdataapi.exception;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message, Throwable ex) {
        super(message, ex);
    }

}
