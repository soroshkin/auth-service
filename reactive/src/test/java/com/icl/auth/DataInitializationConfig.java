package com.icl.auth;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;

@Configuration(proxyBeanMethods = false)
public class DataInitializationConfig {

    @Autowired
    public void initializeDatabase(ConnectionFactory connectionFactory) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource[] scripts = new Resource[]{
                resourceLoader.getResource("classpath:schema.sql"),
                resourceLoader.getResource("classpath:testData.sql")
        };
        new ResourceDatabasePopulator(scripts).execute(connectionFactory).block();
    }
}
