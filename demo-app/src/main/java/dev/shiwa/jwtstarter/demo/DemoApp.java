package dev.shiwa.jwtstarter.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Demo Application.
 *
 * <p>
 * This is a Spring Boot application that provides REST endpoints for
 * authentication and JWT token verification. It is intended for demonstration
 * and testing purposes, showcasing basic login functionality, token generation,
 * and role-based access checks.
 * </p>
 *
 * <p>
 * The application includes the following components:
 * <ul>
 * <li>{@link JwtLoginController} – handles demo login and token generation</li>
 * <li>{@link JwtVerificationController} – provides endpoints to verify tokens
 * and roles</li>
 * </ul>
 * </p>
 *
 * <p>
 * Swagger/OpenAPI is used for automatic API documentation.
 * </p>
 */
@SpringBootApplication
public class DemoApp {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
	SpringApplication.run(DemoApp.class, args);
    }
}