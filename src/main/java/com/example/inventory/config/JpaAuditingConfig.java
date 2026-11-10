package com.example.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Turns on automatic filling of @CreatedDate / @LastModifiedDate fields. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
