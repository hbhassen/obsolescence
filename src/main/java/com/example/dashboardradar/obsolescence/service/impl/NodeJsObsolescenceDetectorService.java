package com.example.dashboardradar.obsolescence.service.impl;

import com.example.dashboardradar.config.ObsolescenceMatrixProperties;
import com.example.dashboardradar.obsolescence.service.AbstractYamlObsolescenceDetectorService;
import java.time.Clock;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class NodeJsObsolescenceDetectorService extends AbstractYamlObsolescenceDetectorService {

    public NodeJsObsolescenceDetectorService(
            ResourceLoader resourceLoader, ObsolescenceMatrixProperties properties, Clock clock) {
        super(resourceLoader, properties.getMatrixBasePath() + "/nodejs.yml", clock);
    }

    @Override
    public String language() {
        return "nodejs";
    }
}
