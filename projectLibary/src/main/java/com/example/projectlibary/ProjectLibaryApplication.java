package com.example.projectlibary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ProjectLibaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectLibaryApplication.class, args);
    }

}
