package uk.gov.companieshouse.pscstatementdataapi.serialization;


import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LocalDateSerializerTest {

    private LocalDateSerializer serializer;

    @Mock
    private JsonGenerator generator;

    @Captor
    private ArgumentCaptor<String> dateString;

    @BeforeEach
    void setUp() {
        serializer = new LocalDateSerializer();
    }

    @Test
    void dateShouldSerialize() throws Exception {
        LocalDate date = LocalDate.of(2020, 1, 1);

        serializer.serialize(date, generator, null);

        verify(generator).writeRawValue(dateString.capture());
        assertEquals(dateString.getValue(), "ISODate(\"2020-01-01T00:00:00.000Z\")");
    }

    @Test
    void assertNullDateReturnsNull() throws Exception {

        serializer.serialize(null, generator, null);

        verify(generator).writeNull();
    }
}
