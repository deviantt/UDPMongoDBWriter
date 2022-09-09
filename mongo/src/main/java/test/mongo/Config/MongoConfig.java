package test.mongo.Config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import static test.mongo.Const.Const.*;

@Configuration
public class MongoConfig
{
    @Bean
    public MongoClient mongo()
    {
        ConnectionString connectionString = new ConnectionString(String.format("mongodb://%s:%s/%s", DB_ADDRESS, DB_PORT, DB_NAME));
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }
    @Bean
    public MongoTemplate mongoTemplate() throws Exception
    {
            return new MongoTemplate(mongo(), DB_NAME);
    }
}