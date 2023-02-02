package uk.gov.companieshouse.pscstatementdataapi.transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class DateTransformerTest {

    private DateTransformer dateTransformer;

    private static final String DELTA_AT = "20180101093435661593";
    private static final String TRANSFORMED_DELTA_AT = "2018-01-01T09:34:35Z";

    @BeforeEach
    public void setUp() {
        dateTransformer = new DateTransformer();
    }

    @Test
    void transformDate(){
        String transformedDate = dateTransformer.transformDate(DELTA_AT);
        assertEquals(transformedDate, TRANSFORMED_DELTA_AT);
    }
}
