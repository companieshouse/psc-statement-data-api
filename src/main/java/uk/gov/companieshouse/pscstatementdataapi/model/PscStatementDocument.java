package uk.gov.companieshouse.pscstatementdataapi.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.psc.Statement;

import javax.persistence.Id;

@Document(collection="#{@environment.getProperty('mongodb.pscStatements.collection.name')}")
public class PscStatementDocument {

    @Id
    private String id;
    private Created created;
    @Field("company_number")
    private String companyNumber;
    private Updated updated;
    @Field("psc_statement_id")
    private String pscStatementId;
    private Statement data;
    @Field("delta_at")
    private String deltaAt;

    public String getId(){
        return id;
    }
    public Created getCreated(){
        return created;
    }
    public String getCompanyNumber() {
        return companyNumber;
    }
    public Updated getUpdated(){
        return updated;
    }
    public String getPscStatementId(){
        return pscStatementId;
    }
    public Statement getData() {
        return data;
    }
    public String getDeltaAt(){
        return  this.deltaAt;
    }

    public void setId(String id){
        this.id = id;
    }
    public void setCreated(Created created){
        this.created = created;
    }
    public void setCompanyNumber(String companyNumber){
        this.companyNumber = companyNumber;
    }
    public void setUpdated(Updated updated){
        this.updated = updated;
    }
    public void setPscStatementId(String pscStatementId){
        this.pscStatementId = pscStatementId;
    }
    public void setData(Statement data) {
        this.data = data;
    }
    public void setDeltaAt(String deltaAt){
        this.deltaAt = deltaAt;
    }

}