package uk.gov.companieshouse.pscstatementdataapi.converter;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.Statement.StatementEnum;

@ExtendWith(MockitoExtension.class)
class EnumWriteConverterTest {

    private EnumWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EnumWriteConverter();
    }

    @Test
    void testConvert() {
        String convertedEnum = converter.convert(StatementEnum.OE_ALL_BO_IDENTIFIED);
        assertEquals("all-beneficial-owners-identified", convertedEnum);
    }

    @Test
    void testFailedConvert() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
    }
}
