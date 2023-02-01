package uk.gov.companieshouse.pscstatementdataapi.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.TypeDescriptor;

import uk.gov.companieshouse.api.psc.Statement.StatementEnum;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class EnumWriteConverterTest {

    @Mock
    private TypeDescriptor typeDescriptor;

    EnumWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EnumWriteConverter();
    }

    @Test
    void testConvert() {
        String convertedEnum = converter.convert(StatementEnum.ALL_BENEFICIAL_OWNERS_IDENTIFIED);
        assertEquals("all-beneficial-owners-identified", convertedEnum);
    }

    @Test
    void testFailedConvert() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
    }
}