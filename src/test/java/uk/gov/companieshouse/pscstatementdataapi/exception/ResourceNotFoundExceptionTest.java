package uk.gov.companieshouse.pscstatementdataapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundException() {
        HttpStatusCode status = HttpStatusCode.valueOf(404);

        ResourceNotFoundException exception = new ResourceNotFoundException(status, "some error happened");

        assertEquals(status, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"some error happened\"", exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionThrown() {
        HttpStatusCode status = HttpStatusCode.valueOf(404);

        ResponseStatusException exception = assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException(status, "some error happened");
        });

        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertEquals(status, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"some error happened\"", exception.getMessage());
    }
}