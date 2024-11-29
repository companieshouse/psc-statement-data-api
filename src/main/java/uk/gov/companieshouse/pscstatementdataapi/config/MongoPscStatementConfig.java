package uk.gov.companieshouse.pscstatementdataapi.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@ConditionalOnProperty(name = "mongodb.transactional", havingValue = "true")
@Configuration
public class MongoPscStatementConfig extends AbstractMongoClientConfiguration {


    private final String databaseName;
    private final String databaseUri;
    private final MongoCustomConversions mongoCustomConversions;

    public MongoPscStatementConfig(@Value("${spring.data.mongodb.name}") String databaseName,
            @Value("${spring.data.mongodb.uri}") String databaseUri,
            MongoCustomConversions mongoCustomConversions) {
        this.databaseName = databaseName;
        this.databaseUri = databaseUri;
        this.mongoCustomConversions = mongoCustomConversions;
    }

    @Override
    protected String getDatabaseName() {
        return this.databaseName;
    }

    protected String getDatabaseUri() {
        return this.databaseUri;
    }

    @Override
    public MongoCustomConversions customConversions() {
        return this.mongoCustomConversions;
    }

    @Override
    public MongoClient mongoClient() {
        final ConnectionString connectionString =
                new ConnectionString(getDatabaseUri());
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }
}
