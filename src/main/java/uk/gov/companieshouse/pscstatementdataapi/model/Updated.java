package uk.gov.companieshouse.pscstatementdataapi.model;

import java.time.LocalDateTime;

public class Updated {

    private LocalDateTime at;
    private String by;
    private String type;

    public LocalDateTime getAt() {
        return at;
    }

    public Updated setAt(LocalDateTime at) {
        this.at = at;
        return this;
    }

    public String getBy(){
        return by;
    }

    public Updated setBy(String by) {
        this.by = by;
        return this;
    }

    public String getType(){
        return type;
    }

    public Updated setType(String type) {
        this.type = type;
        return this;
    }

    public Updated() {
    }

    public Updated(LocalDateTime at) {
        this.at = at;
    }

    public Updated(LocalDateTime at, String by, String type) {
        this.at = at;
        this.by = by;
        this.type = type;
    }

}
