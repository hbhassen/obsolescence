package com.example.dashboardradar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.obsolescence")
public class ObsolescenceMatrixProperties {

    /**
     * Base location of the YAML matrices. Supports Spring Resource semantics (e.g. classpath:obsolescence).
     */
    private String matrixBasePath = "classpath:obsolescence";

    public String getMatrixBasePath() {
        return matrixBasePath;
    }

    public void setMatrixBasePath(String matrixBasePath) {
        this.matrixBasePath = matrixBasePath;
    }
}
