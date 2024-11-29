package uk.gov.companieshouse.pscstatementdataapi.util;

import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;

@Component
public class ResourceChangedRequestMapper {

    private static final String PSC_STATEMENTS_URI = "/company/%s/persons-with-significant-control-statements/%s/internal";
    private static final String CHANGED = "changed";
    private static final String DELETED = "deleted";
    private static final String RESOURCE_KIND = "persons-with-significant-control-statement";

    private final Supplier<Instant> instantSupplier;



    public ResourceChangedRequestMapper(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

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
                .publishedAt(DateTimeUtil.formatPublishedAt(instantSupplier.get()))
                .type(type);
        return new ChangedResource()
                .resourceUri(String.format(PSC_STATEMENTS_URI, request.companyNumber(), request.statementId()))
                .resourceKind(RESOURCE_KIND)
                .event(event)
                .contextId(request.contextId());
    }
}
