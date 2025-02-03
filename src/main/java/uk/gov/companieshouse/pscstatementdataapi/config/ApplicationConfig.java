package uk.gov.companieshouse.pscstatementdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.converter.EnumWriteConverter;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.serialization.LocalDateDeserializer;
import uk.gov.companieshouse.api.serialization.LocalDateSerializer;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementReadConverter;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementWriteConverter;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;

@Configuration
public class ApplicationConfig {
    private final String apiKey;
    private final String kafkaApiUrl;
    private final String metricsApiUrl;
    private final String exemptionsApiUrl;

    public ApplicationConfig(@Value("${api.key}") String apiKey,
            @Value("${kafka.api.url}") String kafkaApiUrl,
            @Value("${metrics.api.url}") String metricsApiUrl,
            @Value("${exemptions.api.url}") String exemptionsApiUrl) {
        this.apiKey = apiKey;
        this.kafkaApiUrl = kafkaApiUrl;
        this.metricsApiUrl = metricsApiUrl;
        this.exemptionsApiUrl = exemptionsApiUrl;
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(Arrays.asList(
                new PscStatementReadConverter(objectMapper, Statement.class),
                new PscStatementWriteConverter(objectMapper),
                new EnumWriteConverter()));
    }

    @Bean
    public Supplier<InternalApiClient> kafkaApiClientSupplier() {
        return () -> buildClient(kafkaApiUrl);
    }

    @Bean
    public Supplier<InternalApiClient> metricsApiClientSupplier() {
        return () -> buildClient(metricsApiUrl);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Bean
    public Supplier<InternalApiClient> exemptionsApiClientSupplier() {
        return () -> buildClient(exemptionsApiUrl);
    }

    private InternalApiClient buildClient(final String url) {
        ApiKeyHttpClient apiKeyHttpClient = new ApiKeyHttpClient(apiKey);
        apiKeyHttpClient.setRequestId(DataMapHolder.getRequestId());

        InternalApiClient internalApiClient = new InternalApiClient(apiKeyHttpClient);
        internalApiClient.setBasePath(url);

        return internalApiClient;
    }

    @Bean
    public Supplier<Instant> instantSupplier() {
        return Instant::now;
    }

    private ObjectMapper mongoDbObjectMapper() {
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
