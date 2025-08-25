package com.payfleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PayFleet Payment Processing System
 * <p>
 * Main application class that bootstraps the entire Spring Boot application.
 * This is the entry point for our enterprise payment system.
 *
 * @SpringBootApplication annotation combines:
 * - @Configuration: Marks this as a configuration class
 * - @EnableAutoConfiguration: Automatically configures Spring based on dependencies
 * - @ComponentScan: Scans for Spring components in this package and sub-packages
 */
@SpringBootApplication
public class PayFleetApplication {

    /**
     * Main method - application entry point
     * When you run this, Spring Boot starts embedded Tomcat server,
     * configures database connections, sets up REST endpoints, etc.
     */
    public static void main(String[] args) {
        SpringApplication.run(PayFleetApplication.class, args);
        System.out.println("🚀 PayFleet Payment System Started Successfully!");
        System.out.println("📊 Dashboard: http://localhost:8080");
        System.out.println("🔍 Health Check: http://localhost:8080/actuator/health");
    }
}
