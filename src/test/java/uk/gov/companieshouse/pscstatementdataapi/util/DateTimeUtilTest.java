package uk.gov.companieshouse.pscstatementdataapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DateTimeUtilTest {

    @Test
    void shouldFormatPublishedAtDate() {
        // given
        Instant now = Instant.parse("2024-09-04T10:52:22.235486Z");
        final String expected = "2024-09-04T10:52:22";

        // when
        final String actual = DateTimeUtil.formatPublishedAt(now);

        // then
        assertEquals(expected, actual);
    }
}
