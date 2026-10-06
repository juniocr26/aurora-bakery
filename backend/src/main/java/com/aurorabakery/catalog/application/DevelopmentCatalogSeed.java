package com.aurorabakery.catalog.application;
import javax.sql.DataSource;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
@Component @Profile("dev") @ConditionalOnProperty(name="app.seed.enabled",havingValue="true")
public class DevelopmentCatalogSeed implements ApplicationRunner {
 private final DataSource dataSource;
 public DevelopmentCatalogSeed(DataSource dataSource){this.dataSource=dataSource;}
 public void run(ApplicationArguments args){new ResourceDatabasePopulator(new ClassPathResource("db/dev/products.sql")).execute(dataSource);}
}
