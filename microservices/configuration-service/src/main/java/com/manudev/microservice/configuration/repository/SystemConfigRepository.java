package com.manudev.microservice.configuration.repository;

import com.manudev.microservice.configuration.entity.SystemConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {
}
