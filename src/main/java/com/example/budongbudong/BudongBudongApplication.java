package com.example.budongbudong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class BudongBudongApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudongBudongApplication.class, args);
    }

}
