package uk.gov.companieshouse.pscstatementdataapi.api;

import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.exception.ServiceUnavailableException;

@Service
public class PscStatementApiService {

    @Autowired
    InternalApiClient internalApiClient;
    @Value("${chs.kafka.api.endpoint}")
    private String chsKafkaApiUrl;
    @Value("${psc-statements.api.resource.changed.uri}")
    private String resourceChangedUri;
    @Value("${psc-statements.api.resource.kind}")
    private String resourceKind;
    private static final String PSC_STATEMENTS_URI = "/company/%s/persons-with-significant-control-statements/%s";
    private static final String CHANGED_EVENT_TYPE = "changed";
    private static final String DELETE_EVENT_TYPE = "deleted";
    @Autowired
    private Logger logger;

    public ApiResponse<Void> invokeChsKafkaApi(String contextId, String companyNumber, String statementId) {
        internalApiClient.setInternalBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost = internalApiClient.privateChangedResourceHandler()
                .postChangedResource(resourceChangedUri, mapChangedResource(contextId, companyNumber, statementId, null));
        return handleApiCall(changedResourcePost);
    }

    public ApiResponse<Void> invokeChsKafkaApiWithDeleteEvent(String contextId, String companyNumber, String statementId, Statement statement) {
        internalApiClient.setInternalBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost = internalApiClient.privateChangedResourceHandler()
                .postChangedResource(resourceChangedUri, mapChangedResource(contextId, companyNumber, statementId, statement));
        return handleApiCall(changedResourcePost);
    }

    private ChangedResource mapChangedResource(String contextId, String companyNumber, String statementId, Statement statement) {
        ChangedResourceEvent event = new ChangedResourceEvent();
        ChangedResource changedResource = new ChangedResource();
        event.setPublishedAt(String.valueOf(OffsetDateTime.now()));
        if (statement != null) {
            event.setType(DELETE_EVENT_TYPE);
            changedResource.setDeletedData(statement);
        } else {
            event.setType(CHANGED_EVENT_TYPE);
        }
        changedResource.setResourceUri(String.format(PSC_STATEMENTS_URI, companyNumber, statementId));
        changedResource.event(event);
        changedResource.setResourceKind(resourceKind);
        changedResource.setContextId(contextId);
        return changedResource;
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost) {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exception) {
            logger.error("Unsuccessful call to /resource-changed endpoint", exception);
            throw new ServiceUnavailableException(exception.getMessage());
        } catch (RuntimeException exception) {
            logger.error("Error occurred while calling /resource-changed endpoint", exception);
            throw exception;
        }
    }
}

