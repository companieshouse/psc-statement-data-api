package uk.gov.companieshouse.pscstatementdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.api.converter.EnumWriteConverter;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.serialization.LocalDateDeserializer;
import uk.gov.companieshouse.api.serialization.LocalDateSerializer;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementReadConverter;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementWriteConverter;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class ApplicationConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions(){
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(Arrays.asList(
                new PscStatementReadConverter(objectMapper, Statement.class),
                new PscStatementWriteConverter(objectMapper),
                new EnumWriteConverter()));
    }

    @Bean
    public InternalApiClient internalApiClient() {
        return ApiSdkManager.getPrivateSDK();
    }

    @Bean
    public CompanyExemptionsApiService companyExemptionsApiService(){
        return new CompanyExemptionsApiService();
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
