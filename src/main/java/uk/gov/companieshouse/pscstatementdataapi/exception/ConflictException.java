package uk.gov.companieshouse.pscstatementdataapi.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
