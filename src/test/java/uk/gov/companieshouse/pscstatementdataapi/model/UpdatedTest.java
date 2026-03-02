package uk.gov.companieshouse.pscstatementdataapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class UpdatedTest {
    @Test
    void testDefaultConstructor() {
        Updated updated = new Updated();
        assertNull(updated.getAt());
        assertNull(updated.getBy());
        assertNull(updated.getType());
    }

    @Test
    void testConstructorWithAt() {
        LocalDateTime now = LocalDateTime.now();
        Updated updated = new Updated(now);
        assertEquals(now, updated.getAt());
        assertNull(updated.getBy());
        assertNull(updated.getType());
    }

    @Test
    void testConstructorWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        String by = "user";
        String type = "updateType";
        Updated updated = new Updated(now, by, type);
        assertEquals(now, updated.getAt());
        assertEquals(by, updated.getBy());
        assertEquals(type, updated.getType());
    }

    @Test
    void testSettersAndGetters() {
        Updated updated = new Updated();
        LocalDateTime at = LocalDateTime.now();
        String by = "admin";
        String type = "manual";
        updated.setAt(at);
        updated.setBy(by);
        updated.setType(type);
        assertEquals(at, updated.getAt());
        assertEquals(by, updated.getBy());
        assertEquals(type, updated.getType());
    }

    @Test
    void testMethodChaining() {
        LocalDateTime at = LocalDateTime.now();
        Updated updated = new Updated()
                .setAt(at)
                .setBy("chainedUser")
                .setType("chainedType");
        assertEquals(at, updated.getAt());
        assertEquals("chainedUser", updated.getBy());
        assertEquals("chainedType", updated.getType());
    }

    @Test
    void testNullAndEmptyValues() {
        Updated updated = new Updated();
        updated.setAt(null);
        updated.setBy("");
        updated.setType(null);
        assertNull(updated.getAt());
        assertEquals("", updated.getBy());
        assertNull(updated.getType());
    }
}