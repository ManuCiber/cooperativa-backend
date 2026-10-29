package com.manudev.microservices.socio;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;

@SpringBootApplication(scanBasePackages = "com.manudev.microservices")
@AutoConfigurationPackage(basePackages = "com.manudev.microservices")
public class SocioServicesApplication{
    public static void main(String[] args) {
        SpringApplication.run(SocioServicesApplication.class, args);
    }
}