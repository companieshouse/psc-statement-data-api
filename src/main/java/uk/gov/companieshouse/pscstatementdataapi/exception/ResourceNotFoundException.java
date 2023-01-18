package uk.gov.companieshouse.pscstatementdataapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {

    String message;
    public ResourceNotFoundException(HttpStatus status, String msg){
        super(status, msg);
        this.message = msg;
    }

    public String getMsg(){
        return message;
    }


}
