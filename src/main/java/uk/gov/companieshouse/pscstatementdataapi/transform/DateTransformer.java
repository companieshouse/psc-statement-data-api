package uk.gov.companieshouse.pscstatementdataapi.transform;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTransformer {

    private final DateTimeFormatter zonedDateTimeformatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneId.of("Z"));
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public String transformDate(String date){
        OffsetDateTime offsetDatetime = ZonedDateTime.parse(date, zonedDateTimeformatter).toOffsetDateTime();
        return offsetDatetime.format(dateTimeFormatter);
    }
}
