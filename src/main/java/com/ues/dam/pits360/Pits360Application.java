package com.ues.dam.pits360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.ues.dam.pits360", "com.ues.dam.integration" })
public class Pits360Application {

	public static void main(String[] args) {
		SpringApplication.run(Pits360Application.class, args);
	}

}
