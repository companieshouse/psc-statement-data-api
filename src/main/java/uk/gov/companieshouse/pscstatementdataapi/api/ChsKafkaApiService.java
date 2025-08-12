package uk.gov.companieshouse.pscstatementdataapi.api;

import static uk.gov.companieshouse.pscstatementdataapi.PSCStatementDataApiApplication.NAMESPACE;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatementdataapi.exception.ServiceUnavailableException;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.util.ResourceChangedRequestMapper;

@Service
public class ChsKafkaApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String RESOURCE_CHANGED_URI = "/private/resource-changed";
    private final ResourceChangedRequestMapper mapper;
    private final Supplier<InternalApiClient> internalApiClientSupplier;

    public ChsKafkaApiService(ResourceChangedRequestMapper mapper,
            @Qualifier("kafkaApiClientSupplier") Supplier<InternalApiClient> internalApiClientSupplier) {
        this.mapper = mapper;
        this.internalApiClientSupplier = internalApiClientSupplier;
    }

    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) {
        PrivateChangedResourcePost changedResourcePost = internalApiClientSupplier.get().privateChangedResourceHandler()
                .postChangedResource(RESOURCE_CHANGED_URI, mapper.mapChangedEvent(resourceChangedRequest));
        return handleApiCall(changedResourcePost);
    }

    public ApiResponse<Void> invokeChsKafkaApiDelete(ResourceChangedRequest resourceChangedRequest) {
        PrivateChangedResourcePost changedResourcePost = internalApiClientSupplier.get().privateChangedResourceHandler()
                .postChangedResource(RESOURCE_CHANGED_URI, mapper.mapDeletedEvent(resourceChangedRequest));
        return handleApiCall(changedResourcePost);
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost) {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exception) {
            LOGGER.error("Unsuccessful call to /resource-changed endpoint",
                    exception, DataMapHolder.getLogMap());
            throw new ServiceUnavailableException(exception.getMessage());
        } catch (RuntimeException exception) {
            LOGGER.error("Error occurred while calling /resource-changed endpoint",
                    exception, DataMapHolder.getLogMap());
            throw exception;
        }
    }
}

