package uk.gov.companieshouse.pscstatementdataapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.model.Updated;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;

@ExtendWith(MockitoExtension.class)
class ResourceChangedRequestMapperTest {

    private static final String CONTEXT_ID = "654321";
    private static final String COMPANY_NUMBER = "companyNumber";
    private static final String STATEMENT_ID = "statementId";
    private static final String RESOURCE_KIND = "persons-with-significant-control-statement";
    private static final String RESOURCE_URI = String.format(
            "/company/%s/persons-with-significant-control-statements/%s/internal",
            COMPANY_NUMBER, STATEMENT_ID);
    private static final String ETAG = "etag";
    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final String PUBLISHED_AT = DateTimeUtil.formatPublishedAt(UPDATED_AT);

    @InjectMocks
    private ResourceChangedRequestMapper mapper;

    @Mock
    private Supplier<Instant> instantSupplier;

    @Test
    void shouldMapChangedEvent() {
        // given
        ResourceChangedTestArgument argument = ResourceChangedTestArgument.builder()
                .withRequest(new ResourceChangedRequest(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID,
                        null, false))
                .withContextId(CONTEXT_ID)
                .withResourceUri(RESOURCE_URI)
                .withResourceKind(RESOURCE_KIND)
                .withEventType("changed")
                .withEventPublishedAt(PUBLISHED_AT)
                .build();
        when(instantSupplier.get()).thenReturn(UPDATED_AT);

        // when
        ChangedResource actual = mapper.mapChangedEvent(argument.request());

        // then
        assertEquals(argument.changedResource(), actual);
    }

    @ParameterizedTest
    @MethodSource("resourceChangedScenarios")
    void shouldMapDeletedEvent(ResourceChangedTestArgument argument) {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);

        // when
        ChangedResource actual = mapper.mapDeletedEvent(argument.request());

        // then
        assertEquals(argument.changedResource(), actual);
    }

    static Stream<ResourceChangedTestArgument> resourceChangedScenarios() {
        return Stream.of(
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID,
                                new PscStatementDocument(), true))
                        .withContextId(CONTEXT_ID)
                        .withResourceUri(RESOURCE_URI)
                        .withResourceKind(RESOURCE_KIND)
                        .withEventType("deleted")
                        .withEventPublishedAt(PUBLISHED_AT)
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(CONTEXT_ID, COMPANY_NUMBER, STATEMENT_ID,
                                getPscStatementDocument(), true))
                        .withContextId(CONTEXT_ID)
                        .withResourceUri(RESOURCE_URI)
                        .withResourceKind(RESOURCE_KIND)
                        .withEventType("deleted")
                        .withDeletedData(getPscStatementData())
                        .withEventPublishedAt(PUBLISHED_AT)
                        .build()
        );
    }

    record ResourceChangedTestArgument(ResourceChangedRequest request, ChangedResource changedResource) {

        public static ResourceChangedTestArgumentBuilder builder() {
            return new ResourceChangedTestArgumentBuilder();
        }

    }

    static class ResourceChangedTestArgumentBuilder {

        private ResourceChangedRequest request;
        private String resourceUri;
        private String resourceKind;
        private String contextId;
        private String eventType;
        private String eventPublishedAt;
        private Object deletedData;

        public ResourceChangedTestArgumentBuilder withRequest(ResourceChangedRequest request) {
            this.request = request;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withResourceUri(String resourceUri) {
            this.resourceUri = resourceUri;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withResourceKind(String resourceKind) {
            this.resourceKind = resourceKind;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withContextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withEventPublishedAt(String eventPublishedAt) {
            this.eventPublishedAt = eventPublishedAt;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withDeletedData(Object deletedData) {
            this.deletedData = deletedData;
            return this;
        }

        public ResourceChangedTestArgument build() {
            ChangedResource changedResource = new ChangedResource();
            changedResource.setResourceUri(this.resourceUri);
            changedResource.setResourceKind(this.resourceKind);
            changedResource.setContextId(this.contextId);
            ChangedResourceEvent event = new ChangedResourceEvent();
            event.setType(this.eventType);
            event.setPublishedAt(this.eventPublishedAt);
            changedResource.setEvent(event);
            changedResource.setDeletedData(deletedData);
            return new ResourceChangedTestArgument(this.request, changedResource);
        }
    }

    private static PscStatementDocument getPscStatementDocument() {
        PscStatementDocument document = new PscStatementDocument();
        document.setUpdated(new Updated().setAt(LocalDateTime.ofInstant(UPDATED_AT, ZoneId.of("Z"))));
        document.setData(getPscStatementData());
        return document;
    }

    private static Statement getPscStatementData() {
        Statement statement = new Statement();
        statement.setEtag(ETAG);
        statement.setNotifiedOn(LocalDate.ofInstant(UPDATED_AT, ZoneId.of("Z")));
        statement.setKind(Statement.KindEnum.PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT);
        statement.setStatement(Statement.StatementEnum.NO_INDIVIDUAL_OR_ENTITY_WITH_SIGNFICANT_CONTROL);
        return statement;
    }

}
