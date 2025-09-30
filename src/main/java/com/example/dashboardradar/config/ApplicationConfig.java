package com.example.dashboardradar.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({GithubProperties.class, StructureRulesProperties.class, ObsolescenceMatrixProperties.class})
public class ApplicationConfig {
}
