package dev.cat.modular.monolith.config;

import javax.sql.DataSource;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableProcessApplication
public class CamundaDatasourceCofig {
    
    @Bean(name = "camundaBpmDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.camunda")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "camundaBpmTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("camundaBpmDataSource") DataSource dataSource ) {
        return new DataSourceTransactionManager(dataSource);
    }
}
