package uk.gov.companieshouse.pscstatementdataapi.util;

import java.util.Objects;
import java.util.Optional;
import uk.gov.companieshouse.api.model.PscStatementDocument;

public record ResourceChangedRequest(String contextId, String companyNumber, String statementId, Optional<PscStatementDocument> document, boolean isDelete) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceChangedRequest that = (ResourceChangedRequest) o;
        return isDelete() == that.isDelete() && Objects.equals(contextId, that.contextId)
                && Objects.equals(statementId, that.statementId) && Objects.equals(companyNumber,
                that.companyNumber) && Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, companyNumber, statementId, document, isDelete());
    }
}
