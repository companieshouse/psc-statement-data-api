package uk.gov.companieshouse.pscstatementdataapi.api;

import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.logging.Logger;

@Service
public class PscStatementApiService {

    @Autowired
    InternalApiClient internalApiClient;
    @Value("${psc-statements.api.resource.changed.uri}")
    private String resourceChangedUri;
    @Value("${psc-statements.api.resource.kind}")
    private String resourceKind;
    private static final String PSC_STATEMENTS_URI = "/company/%s/persons-with-significant-control-statements/%s";
    private static final String DELETE_EVENT_TYPE = "deleted";
    @Autowired
    private Logger logger;

    public ApiResponse<Void> invokeChsKafkaApiWithDeleteEvent(String contextId, String companyNumber, String statementId, Statement statement) {
        try {
            PrivateChangedResourcePost changedResourcePost = internalApiClient.privateChangedResourceHandler()
                    .postChangedResource(resourceChangedUri, mapChangedResource(contextId, companyNumber, statementId, statement));
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exp) {
            HttpStatus statusCode = HttpStatus.valueOf(exp.getStatusCode());
            logger.error("Unsuccessful call to /resource-changed endpoint for a Psc Statement delete event", exp);
            throw new ResponseStatusException(statusCode, exp.getMessage());
        }
    }

    private ChangedResource mapChangedResource(String contextId, String companyNumber, String statementId, Statement statement) {
        ChangedResourceEvent event = new ChangedResourceEvent();
        ChangedResource changedResource = new ChangedResource();
        event.setPublishedAt(String.valueOf(OffsetDateTime.now()));
        event.setType(DELETE_EVENT_TYPE);
        changedResource.setDeletedData(statement);
        changedResource.setResourceUri(String.format(PSC_STATEMENTS_URI + "/internal", companyNumber, statementId));
        changedResource.event(event);
        changedResource.setResourceKind(resourceKind);
        changedResource.setContextId(contextId);
        return changedResource;
    }
}

