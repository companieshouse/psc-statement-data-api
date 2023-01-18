package uk.gov.companieshouse.pscstatementdataapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(HttpStatus status, String msg){
        super(status, msg);
    }
}
