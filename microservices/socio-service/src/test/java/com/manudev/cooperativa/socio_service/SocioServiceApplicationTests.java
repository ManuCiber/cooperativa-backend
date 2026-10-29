package com.manudev.cooperativa.socio_service;

import com.manudev.microservices.socio.SocioServicesApplication;
import com.manudev.microservices.socio.repository.SocioRepository;
import com.manudev.microservices.directiva.repository.DirectivaRepository;
import com.manudev.microservices.directiva.controller.DirectivaController;
import com.manudev.microservices.directiva.service.IDirectivaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SocioServicesApplication.class, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.compatibility-verifier.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class SocioServiceApplicationTests {
    @MockitoBean SocioRepository socios;
    @MockitoBean DirectivaRepository directivas;
    @Autowired ApplicationContext context;

    @Test void contextLoadsWithDirectivaControllerAndService() {
        assertNotNull(context.getBean(DirectivaController.class));
        assertNotNull(context.getBean(IDirectivaService.class));
    }
}
