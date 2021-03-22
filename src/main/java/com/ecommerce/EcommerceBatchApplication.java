package com.ecommerce;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestClientException;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAutoConfiguration
@ComponentScan("com.ecommerce")
public class EcommerceBatchApplication {

	public static void main(String[] args) throws RestClientException, IOException {
		SpringApplication.run(EcommerceBatchApplication.class, args);

	}

}
