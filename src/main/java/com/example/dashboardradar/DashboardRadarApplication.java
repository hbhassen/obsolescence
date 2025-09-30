package com.example.dashboardradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DashboardRadarApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardRadarApplication.class, args);
    }
}
