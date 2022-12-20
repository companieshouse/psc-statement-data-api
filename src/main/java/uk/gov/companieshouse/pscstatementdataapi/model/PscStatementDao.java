package uk.gov.companieshouse.pscstatementdataapi.model;

import java.util.Date;
import uk.gov.companieshouse.api.psc.Statement;

public class PscStatementDao {

    private Statement data;

    public Statement getData() {
        return data;
    }

    public void setData(Statement data) {
        this.data = data;
    }
}