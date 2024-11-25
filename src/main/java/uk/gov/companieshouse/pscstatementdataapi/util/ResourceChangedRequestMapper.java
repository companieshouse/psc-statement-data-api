package uk.gov.companieshouse.pscstatementdataapi.util;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;

public class ResourceChangedRequestMapper {

    private static final String PSC_STATEMENTS_URI = "/company/%s/persons-with-significant-control-statements/%s";
    private static final String CHANGED = "changed";
    private static final String DELETED = "deleted";

    @Value("${chs.api.kafka.kind}")
    private String resourceKind;

    public ChangedResource mapChangedEvent(ResourceChangedRequest request) {
        return buildChangedResource(CHANGED, request);
    }

    public ChangedResource mapDeletedEvent(ResourceChangedRequest request) {
        ChangedResource changedResource = buildChangedResource(DELETED, request);
        changedResource.setDeletedData(request.document().getData());
        return changedResource;
    }

    private ChangedResource buildChangedResource(final String type, ResourceChangedRequest request){
        ChangedResourceEvent event = new ChangedResourceEvent()
                .publishedAt(DateTimeUtil.formatPublishedAt(Instant.now()))
                .type(type);
        return new ChangedResource()
                .resourceUri(String.format(PSC_STATEMENTS_URI, request.companyNumber(), request.statementId()))
                .resourceKind(resourceKind)
                .event(event)
                .contextId(request.contextId());
    }
}
