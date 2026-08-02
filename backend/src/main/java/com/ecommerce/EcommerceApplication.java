package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the E-Commerce Platform backend.
 *
 * Architecture: Layered (Controller → Service → Repository → Entity)
 * Patterns:    Singleton (Spring beans), Factory, Strategy, Observer, State, etc.
 */
@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
