package uk.gov.companieshouse.pscstatementdataapi.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ConditionalOnProperty(name = "mongodb.transactional", havingValue = "true")
@Configuration
public class MongoPscStatementConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.name}")
    private String databaseName;

    @Value("${spring.data.mongodb.uri}")
    private String databaseUri;

    @Autowired
    MongoCustomConversions mongoCustomConversions;

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
