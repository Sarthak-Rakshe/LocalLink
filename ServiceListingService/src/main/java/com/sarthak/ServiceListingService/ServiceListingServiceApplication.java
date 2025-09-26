package com.sarthak.ServiceListingService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceListingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceListingServiceApplication.class, args);
	}

}
