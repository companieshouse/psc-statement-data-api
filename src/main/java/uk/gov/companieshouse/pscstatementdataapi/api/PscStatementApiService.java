package uk.gov.companieshouse.pscstatementdataapi.api;

import static uk.gov.companieshouse.pscstatementdataapi.PSCStatementDataApiApplication.NAMESPACE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.util.ResourceChangedRequestMapper;

@Service
public class PscStatementApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final ResourceChangedRequestMapper mapper;
    private final InternalApiClient internalApiClient;
    private final String chsKafkaApiUrl;
    private final String resourceChangedUri;

    public PscStatementApiService(ResourceChangedRequestMapper mapper, InternalApiClient internalApiClient,
            @Value("${chs.api.kafka.url}") String chsKafkaApiUrl,
            @Value("${chs.api.kafka.uri}") String resourceChangedUri) {
        this.mapper = mapper;
        this.internalApiClient = internalApiClient;
        this.chsKafkaApiUrl = chsKafkaApiUrl;
        this.resourceChangedUri = resourceChangedUri;
    }


    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) {
        internalApiClient.setBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost = internalApiClient.privateChangedResourceHandler()
                .postChangedResource(resourceChangedUri, mapper.mapChangedEvent(resourceChangedRequest));
        return handleApiCall(changedResourcePost);
    }

    public ApiResponse<Void> invokeChsKafkaApiDelete(ResourceChangedRequest resourceChangedRequest) {
        internalApiClient.setBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost = internalApiClient.privateChangedResourceHandler()
                .postChangedResource(resourceChangedUri, mapper.mapDeletedEvent(resourceChangedRequest));
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

