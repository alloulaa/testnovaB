package com.example.testnova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class TestNovaApplication {
    public static void main(String[] args) {
        System.out.println("=== Démarrage de TestNova Backend ===");
        SpringApplication.run(TestNovaApplication.class, args);
        System.out.println("=== Backend prêt sur http://localhost:8081 ===");
    }
}
