package uk.gov.companieshouse.pscstatementdataapi.config;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.pscstatementdataapi.converter.EnumWriteConverter;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementReadConverter;
import uk.gov.companieshouse.pscstatementdataapi.converter.PscStatementWriteConverter;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.serdes.LocalDateDeserializer;
import uk.gov.companieshouse.pscstatementdataapi.serdes.LocalDateSerializer;

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
        JsonMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(Arrays.asList(
                new PscStatementReadConverter(objectMapper),
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
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
                // No module registration needed; java.time support is native in Jackson 3!
                .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
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

    private JsonMapper mongoDbObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());

        return JsonMapper.builder()
                .enable(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL))
                .addModule(module)
                .build();
    }
}
