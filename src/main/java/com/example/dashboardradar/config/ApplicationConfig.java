package com.example.dashboardradar.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        GithubProperties.class,
        GitlabProperties.class,
        StructureRulesProperties.class,
        ObsolescenceMatrixProperties.class,
        GitFlowProperties.class,
        SourceControlProperties.class
})
public class ApplicationConfig {
}
