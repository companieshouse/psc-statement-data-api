package uk.gov.companieshouse.pscstatementdataapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.psc.Statement;

@Document(collection = "#{@environment.getProperty('mongodb.pscStatements.collection.name')}")
public class PscStatementDocument {

    @Id
    @JsonProperty("_id")
    private String id;
    private Created created;
    @Field("company_number")
    @JsonProperty("company_number")
    private String companyNumber;
    private Updated updated;
    @Field("psc_statement_id_raw")
    @JsonProperty("psc_statement_id_raw")
    private String pscStatementIdRaw;
    private Statement data;
    @Field("delta_at")
    @JsonProperty("delta_at")
    private String deltaAt;

    public String getId() {
        return id;
    }

    public Created getCreated() {
        return created;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public Updated getUpdated() {
        return updated;
    }

    public String getPscStatementIdRaw() {
        return pscStatementIdRaw;
    }

    public Statement getData() {
        return data;
    }

    public String getDeltaAt() {
        return this.deltaAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreated(Created created) {
        this.created = created;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public void setUpdated(Updated updated) {
        this.updated = updated;
    }

    public void setPscStatementIdRaw(String pscStatementIdRaw) {
        this.pscStatementIdRaw = pscStatementIdRaw;
    }

    public void setData(Statement data) {
        this.data = data;
    }

    public void setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PscStatementDocument that = (PscStatementDocument) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getCreated(), that.getCreated())
                && Objects.equals(getCompanyNumber(), that.getCompanyNumber()) && Objects.equals(
                getUpdated(), that.getUpdated()) && Objects.equals(getPscStatementIdRaw(), that.getPscStatementIdRaw())
                && Objects.equals(getData(), that.getData()) && Objects.equals(getDeltaAt(),
                that.getDeltaAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreated(), getCompanyNumber(), getUpdated(), getPscStatementIdRaw(), getData(),
                getDeltaAt());
    }
}