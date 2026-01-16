package de.aschwartz.camunda7demo.realestatefinancing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the real estate financing demo application.
 */
@SpringBootApplication
public class RealEstateFinancingApplication {
	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(RealEstateFinancingApplication.class, args);
	}
}
