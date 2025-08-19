package uk.gov.companieshouse.pscstatementdataapi.util;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

public class DateTimeUtil {

    private static final DateTimeFormatter publishedAtDateTimeFormatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter deltaAtFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);

    private DateTimeUtil() {
    }

    public static String formatPublishedAt(Instant now) {
        return publishedAtDateTimeFormatter.format(now.atZone(UTC));
    }

    public static boolean isDeltaStale(final String requestDeltaAt, final String existingDeltaAt) {
        return StringUtils.isNotBlank(existingDeltaAt) && OffsetDateTime.parse(requestDeltaAt, deltaAtFormatter)
                .isBefore(OffsetDateTime.parse(existingDeltaAt, deltaAtFormatter));
    }
}
