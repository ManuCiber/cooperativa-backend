package com.manudev.cooperativa.prestamo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrestamoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrestamoServiceApplication.class, args);
	}

}
