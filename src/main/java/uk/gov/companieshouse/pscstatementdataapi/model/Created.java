package uk.gov.companieshouse.pscstatementdataapi.model;

import java.time.LocalDateTime;

public class Created {

    private LocalDateTime at;
    private String by;

    public LocalDateTime getAt() {
        return at;
    }

    public Created setAt(LocalDateTime at) {
        this.at = at;
        return this;
    }

    public String getBy(){
        return by;
    }

    public Created setBy(String by) {
        this.by = by;
        return this;
    }

}