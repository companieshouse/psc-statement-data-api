package uk.gov.companieshouse.pscstatementdataapi.api;

import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.PscStatementDocument;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.util.DateTimeUtil;
import uk.gov.companieshouse.pscstatementdataapi.util.ResourceChangedRequest;

@Service
public class PscStatementApiService {

    @Autowired
    InternalApiClient internalApiClient;
    @Value("${chs.api.kafka.url}")
    private String chsKafkaApiUrl;
    @Value("${chs.api.kafka.uri}")
    private String resourceChangedUri;
    @Value("${chs.api.kafka.kind}")
    private String resourceKind;
    private static final String PSC_STATEMENTS_URI = "/company/%s/persons-with-significant-control-statements/%s";
    private static final String CHANGED_EVENT_TYPE = "changed";
    private static final String DELETE_EVENT_TYPE = "deleted";
    @Autowired
    private Logger logger;

    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) {
        internalApiClient.setBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost = internalApiClient.privateChangedResourceHandler()
                .postChangedResource(resourceChangedUri, mapChangedResource(resourceChangedRequest));
        return handleApiCall(changedResourcePost);
    }

    private ChangedResource mapChangedResource(ResourceChangedRequest request) {
        boolean isDelete = request.isDelete();

        ChangedResourceEvent event = new ChangedResourceEvent()
                .publishedAt(DateTimeUtil.formatPublishedAt(Instant.now()))
                .type(isDelete ? DELETE_EVENT_TYPE : CHANGED_EVENT_TYPE);

        ChangedResource changedResource = new ChangedResource()
                .resourceUri(String.format(PSC_STATEMENTS_URI, request.companyNumber(), request.statementId()))
                .resourceKind(resourceKind)
                .event(event)
                .contextId(request.contextId());

        Optional<PscStatementDocument> document = request.document();
        if (isDelete && document.isPresent()){
            Statement statement = document.get().getData();
            changedResource.setDeletedData(statement);
        }

        return changedResource;
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

