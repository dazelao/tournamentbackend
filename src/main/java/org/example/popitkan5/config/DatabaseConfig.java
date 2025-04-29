package org.example.popitkan5.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
//@EnableJpaRepositories(basePackages = {"org.example.popitkan5.repository", "org.example.popitkan5.games.repository"})
//@EnableJpaRepositories(basePackages = {"org.example.popitkan5.tournament.repository", "org.example.popitkan5.games.repository"})
@EnableTransactionManagement
public class DatabaseConfig {
    // Конфигурация базы данных
}
