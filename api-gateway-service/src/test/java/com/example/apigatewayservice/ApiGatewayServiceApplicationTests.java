package com.example.apigatewayservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ApiGatewayServiceApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
		// Simple test to ensure the application context loads successfully.
		assertNotNull(context, "Application context should not be null.");
	}

	@Test
	void mainMethod_shouldRun() {
		// This test indirectly covers the main method by ensuring the application starts.
		// For a more direct test of the main method's logic (like active profiles logging),
		// you might need a more complex setup or to capture System.out.
		// However, for basic coverage, contextLoads is often sufficient.
		ApiGatewayServiceApplication.main(new String[]{"--server.port=0"});
	}

}
