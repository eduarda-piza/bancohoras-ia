package com.bancohoras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BancoHorasApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoHorasApplication.class, args);
    }
}
