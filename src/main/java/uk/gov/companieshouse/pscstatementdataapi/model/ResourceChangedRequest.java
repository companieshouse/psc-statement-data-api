package uk.gov.companieshouse.pscstatementdataapi.model;

import java.util.Objects;

public record ResourceChangedRequest(String contextId, String companyNumber, String statementId,
                                     PscStatementDocument document, boolean isDelete) {

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
