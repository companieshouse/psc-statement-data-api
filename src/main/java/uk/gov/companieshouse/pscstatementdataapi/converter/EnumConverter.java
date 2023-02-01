package uk.gov.companieshouse.pscstatementdataapi.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.Set;

public class EnumConverter {
    @WritingConverter
    public static class EnumToString implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(Enum.class, String.class));
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            try {
                return sourceType.getType().getDeclaredMethod("getValue", null)
                        .invoke(source, null);
            } catch (Exception ex) {
                return ((Enum<?>) source).name();
            }
        }
    }
}
