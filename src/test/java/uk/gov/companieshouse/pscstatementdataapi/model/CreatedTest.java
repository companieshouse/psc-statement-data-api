package uk.gov.companieshouse.pscstatementdataapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CreatedTest {
    @Test
    void testDefaultValues() {
        Created created = new Created();
        assertNull(created.getAt());
        assertNull(created.getBy());
    }

    @Test
    void testSettersAndGetters() {
        Created created = new Created();
        LocalDateTime now = LocalDateTime.now();
        String by = "user";
        created.setAt(now);
        created.setBy(by);
        assertEquals(now, created.getAt());
        assertEquals(by, created.getBy());
    }

    @Test
    void testMethodChaining() {
        LocalDateTime now = LocalDateTime.now();
        Created created = new Created()
            .setAt(now)
            .setBy("admin");
        assertEquals(now, created.getAt());
        assertEquals("admin", created.getBy());
    }

    @Test
    void testNullAndEmptyValues() {
        Created created = new Created();
        created.setAt(null);
        created.setBy("");
        assertNull(created.getAt());
        assertEquals("", created.getBy());
    }
}