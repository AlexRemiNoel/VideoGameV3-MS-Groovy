package com.example.apigatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ApiGatewayServiceApplication {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ApiGatewayServiceApplication.class, args);
		System.out.println("Active Profile(s):" + String.join(",", context.getEnvironment().getActiveProfiles()));
	}

}
