package uk.gov.companieshouse.pscstatementdataapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundException() {
        HttpStatusCode status = HttpStatusCode.valueOf(404);
        String message =  "404 NOT_FOUND";

        ResourceNotFoundException exception = new ResourceNotFoundException(status, any());

        assertEquals(status, exception.getStatusCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionThrown() {
        HttpStatusCode status = HttpStatusCode.valueOf(404);
        String message =  "404 NOT_FOUND";

        ResponseStatusException exception = assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException(status, any());
        });

        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertEquals(status, exception.getStatusCode());
        assertEquals(message, exception.getMessage());
    }
}
