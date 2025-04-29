package org.example.popitkan5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "org.example.popitkan5")
@EntityScan(basePackages = "org.example.popitkan5")
@EnableJpaRepositories(basePackages = "org.example.popitkan5")
public class PopitkaN5Application {

    public static void main(String[] args) {
        SpringApplication.run(PopitkaN5Application.class, args);
    }

}
