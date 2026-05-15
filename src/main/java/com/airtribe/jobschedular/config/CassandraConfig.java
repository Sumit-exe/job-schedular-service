package com.airtribe.jobschedular.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;

import java.nio.file.Paths;

@Configuration
public class CassandraConfig {

    @Value("${datastax.astra.secure-connect-bundle}")
    private String secureConnectBundle;

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspace;

    @Value("${spring.data.cassandra.username}")
    private String username;

    @Value("${spring.data.cassandra.password}")
    private String password;

    @Bean
    @Primary
    public CqlSession cqlSession() {
        return CqlSession.builder()
                .withCloudSecureConnectBundle(Paths.get(secureConnectBundle))
                .withAuthCredentials(username, password)
                .withKeyspace(keyspace)
                .build();
    }

    @Bean
    public CassandraMappingContext cassandraMappingContext() {
        return new CassandraMappingContext();
    }

    @Bean
    public CassandraConverter cassandraConverter() {
        return new MappingCassandraConverter(cassandraMappingContext());
    }

    @Bean
    public CassandraOperations cassandraTemplate() {
        return new CassandraTemplate(cqlSession(), cassandraConverter());
    }
}