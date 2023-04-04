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
    public ResponseEntity<String> handleNotFoundException(ResourceNotFoundException exception){
        logger.error(exception.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        logger.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception exception) {
        logger.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
