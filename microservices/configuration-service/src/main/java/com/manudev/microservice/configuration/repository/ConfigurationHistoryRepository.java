package com.manudev.microservice.configuration.repository;

import com.manudev.microservice.configuration.entity.ConfigurationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigurationHistoryRepository extends MongoRepository<ConfigurationHistory, String> {
    Page<ConfigurationHistory> findAllByOrderByChangedAtDesc(Pageable pageable);
}
