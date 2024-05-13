package uk.gov.companieshouse.pscstatementdataapi.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.MethodNotAllowedException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.api.exception.BadRequestException;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import java.time.format.DateTimeParseException;

@ControllerAdvice
public class ExceptionHandlerConfig {

    @Autowired
    Logger logger;

    @ExceptionHandler(value= ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(ResourceNotFoundException exception){
        logger.error(exception.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(value = {ServiceUnavailableException.class, DataAccessException.class, MongoException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(Exception exception) {
        logger.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = {BadRequestException.class, DateTimeParseException.class,
            HttpMessageNotReadableException.class, JsonProcessingException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception exception) {
        logger.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MethodNotAllowedException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleMethodNotAllowedException(Exception exception) {
        logger.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception exception) {
        logger.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
