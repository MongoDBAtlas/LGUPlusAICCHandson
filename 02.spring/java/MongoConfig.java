package com.mongodb.spring.lgu.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {
    @Autowired
    private MongoDatabaseFactory mongoFactory;
    //private MongoDbFactory mongoDbFactory;

    @Autowired
    private MongoMappingContext mongoMappingContext;
    

    @Bean
    public MappingMongoConverter mappingMongoConverter() {

        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver,mongoMappingContext);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return converter;
    }
    
    @Value("${spring.data.mongodb.uri}")
    private String mongodb_uri;

    @Bean
    public MongoTemplate mongoTemplate()  {
    	
    	ServerApi serverApi = ServerApi.builder()
    	        .version(ServerApiVersion.V1)
    	        .build();
    	
    	MongoClientSettings settings = MongoClientSettings.builder()
    	        .applyConnectionString(new ConnectionString(mongodb_uri))
    	        .serverApi(serverApi)
    	        .build();
    	
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(mongoClient, "handson");
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(factory), new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return new MongoTemplate(factory, converter);
    }    
    
}