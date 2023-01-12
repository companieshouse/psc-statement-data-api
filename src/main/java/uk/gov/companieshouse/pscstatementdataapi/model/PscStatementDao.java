package uk.gov.companieshouse.pscstatementdataapi.model;

import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.psc.Statement;

@Document(collection="#{@environment.getProperty('mongodb.pscStatements.collection.name')}")
public class PscStatementDao {

    private Statement data;

    public Statement getData() {
        return data;
    }

    public void setData(Statement data) {
        this.data = data;
    }
}