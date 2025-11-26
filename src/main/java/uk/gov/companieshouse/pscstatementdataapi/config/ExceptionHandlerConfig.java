package uk.gov.companieshouse.pscstatementdataapi.config;

import static uk.gov.companieshouse.pscstatementdataapi.PSCStatementDataApiApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoException;
import java.time.format.DateTimeParseException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.MethodNotAllowedException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadGatewayException;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ConflictException;
import uk.gov.companieshouse.pscstatementdataapi.exception.PscStatementConversionException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.exception.SerDesException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ServiceUnavailableException;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;

@ControllerAdvice
public class ExceptionHandlerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(ResourceNotFoundException exception) {
        LOGGER.error(exception.getMessage(), DataMapHolder.getLogMap());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(value = {ServiceUnavailableException.class, DataAccessException.class, MongoException.class,
            BadGatewayException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(Exception exception) {
        LOGGER.error(exception.getMessage(), DataMapHolder.getLogMap());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = {BadRequestException.class, DateTimeParseException.class,
            HttpMessageNotReadableException.class, JsonProcessingException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception exception) {
        LOGGER.error(exception.getMessage(), DataMapHolder.getLogMap());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MethodNotAllowedException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleMethodNotAllowedException(Exception exception) {
        LOGGER.error(exception.getMessage(), DataMapHolder.getLogMap());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<Object> handleConflictException(Exception exception) {
        LOGGER.error(exception.getMessage(), DataMapHolder.getLogMap());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {Exception.class, SerDesException.class, PscStatementConversionException.class})
    public ResponseEntity<String> handleException(Exception exception) {
        LOGGER.error(exception.getMessage(), DataMapHolder.getLogMap());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
