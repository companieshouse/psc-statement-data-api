package uk.gov.companieshouse.pscstatementdataapi.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(HttpStatusCode status, String msg) {
        super(status, msg);
    }
}