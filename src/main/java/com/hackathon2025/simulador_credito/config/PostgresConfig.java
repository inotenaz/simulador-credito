package com.hackathon2025.simulador_credito.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.hackathon2025.simulador_credito.repository.ProdutoRepository;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.hackathon2025.simulador_credito.repository",
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ProdutoRepository.class)
)
public class PostgresConfig {
    
    @Primary
    @Bean
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://localhost:5432/simulador_credito")
            .username("postgres")
            .password("postgres")
            .driverClassName("org.postgresql.Driver")
            .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(postgresDataSource());
        em.setPackagesToScan("com.hackathon2025.simulador_credito.model");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);
        
        return em;
    }
}