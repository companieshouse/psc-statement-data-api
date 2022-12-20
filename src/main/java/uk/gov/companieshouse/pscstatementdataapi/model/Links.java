package uk.gov.companieshouse.pscstatementdataapi.model;

public class Links {

    private String person_with_significant_control;
    private String self;

    public String getPerson_with_significant_control() {
        return person_with_significant_control;
    }

    public void setPerson_with_significant_control(String person_with_significant_control) {
        this.person_with_significant_control = person_with_significant_control;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }
}
