package uk.gov.companieshouse.pscstatementdataapi.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    static DateTimeFormatter publishedAtDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DateTimeUtil() {}

    public static String formatPublishedAt(Instant now) {
        return publishedAtDateTimeFormatter.format(now.atZone(ZoneOffset.UTC));
    }
}
