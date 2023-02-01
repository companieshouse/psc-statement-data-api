package uk.gov.companieshouse.pscstatementdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.pscstatementdataapi.converter.EnumWriteConverter;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementReadConverter;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementWriteConverter;
import uk.gov.companieshouse.pscstatementdataapi.serialization.LocalDateDeserializer;
import uk.gov.companieshouse.pscstatementdataapi.serialization.LocalDateSerializer;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class ApplicationConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions(){
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(Arrays.asList(
                new PscStatementReadConverter(objectMapper),
                new PscStatementWriteConverter(objectMapper),
                new EnumWriteConverter()));
    }

    private ObjectMapper mongoDbObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
