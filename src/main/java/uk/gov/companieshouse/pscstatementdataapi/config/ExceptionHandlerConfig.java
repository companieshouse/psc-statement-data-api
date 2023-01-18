package uk.gov.companieshouse.pscstatementdataapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;

@ControllerAdvice
public class ExceptionHandlerConfig {

    @Autowired
    Logger logger;

    @ExceptionHandler(value= ResourceNotFoundException.class)
    public ResponseEntity<String> handleException(ResourceNotFoundException exception){
        logger.error(exception.getMsg());
        return new ResponseEntity<String>("Resource not found", HttpStatus.NOT_FOUND);
    }

}
