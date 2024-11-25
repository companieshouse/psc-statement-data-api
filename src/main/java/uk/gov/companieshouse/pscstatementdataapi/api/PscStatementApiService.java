package uk.gov.companieshouse.pscstatementdataapi.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.util.ResourceChangedRequestMapper;

@Service
public class PscStatementApiService {

    private final ResourceChangedRequestMapper mapper;
    private final InternalApiClient internalApiClient;
    private final Logger logger;

    @Value("${chs.api.kafka.url}")
    private String chsKafkaApiUrl;
    @Value("${chs.api.kafka.uri}")
    private String resourceChangedUri;

    public PscStatementApiService(ResourceChangedRequestMapper mapper, InternalApiClient internalApiClient,
            Logger logger) {
        this.mapper = mapper;
        this.internalApiClient = internalApiClient;
        this.logger = logger;
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
            logger.error("Unsuccessful call to /resource-changed endpoint",
                    exception, DataMapHolder.getLogMap());
            throw new ServiceUnavailableException(exception.getMessage());
        } catch (RuntimeException exception) {
            logger.error("Error occurred while calling /resource-changed endpoint",
                    exception, DataMapHolder.getLogMap());
            throw exception;
        }
    }
}

