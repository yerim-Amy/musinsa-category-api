package com.musinsa.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MusinsaCategoryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusinsaCategoryApiApplication.class, args);
    }

}
