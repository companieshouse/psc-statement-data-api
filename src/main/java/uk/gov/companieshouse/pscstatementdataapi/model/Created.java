package uk.gov.companieshouse.pscstatementdataapi.model;

import java.time.LocalDateTime;

public class Created {

    private LocalDateTime at;

    public LocalDateTime getAt() {
        return at;
    }

    public Created setAt(LocalDateTime at) {
        this.at = at;
        return this;
    }
}