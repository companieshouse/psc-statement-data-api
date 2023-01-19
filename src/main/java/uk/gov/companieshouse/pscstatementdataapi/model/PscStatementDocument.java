package uk.gov.companieshouse.pscstatementdataapi.model;

import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.psc.Statement;

import javax.persistence.Id;

@Document(collection="#{@environment.getProperty('mongodb.pscStatements.collection.name')}")
public class PscStatementDocument {

    @Id
    private String id;
    private Statement data;

    public String getId() {
        return id;
    }
    public Statement getData() {
        return data;
    }

    public PscStatementDocument setId(String id) {
        this.id = id;
        return this;
    }
    public void setData(Statement data) {
        this.data = data;
    }
}