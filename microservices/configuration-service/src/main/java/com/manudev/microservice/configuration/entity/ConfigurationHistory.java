package com.manudev.microservice.configuration.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "configuration_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationHistory {
    @Id
    private String id;
    private String action;
    private String section;
    private String actor;
    private Instant changedAt;
    private SystemConfig before;
    private SystemConfig after;

}
