package com.example.dashboardradar.batch;

import com.example.dashboardradar.model.ComplianceReport;
import com.example.dashboardradar.model.ObsolescenceReport;
import com.example.dashboardradar.model.ProjectAudit;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.service.ComplianceCheckerService;
import com.example.dashboardradar.service.ObsolescenceDetectorService;
import com.example.dashboardradar.service.PersistenceService;
import com.example.dashboardradar.service.ProjectScanner;
import com.example.dashboardradar.service.ProviderSelectionManager;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class AuditTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditTasklet.class);

    private final ProjectScanner projectScanner;
    private final ComplianceCheckerService complianceCheckerService;
    private final ObsolescenceDetectorService obsolescenceDetectorService;
    private final PersistenceService persistenceService;
    private final ProviderSelectionManager providerSelectionManager;

    public AuditTasklet(ProjectScanner projectScanner,
            ComplianceCheckerService complianceCheckerService,
            ObsolescenceDetectorService obsolescenceDetectorService,
            PersistenceService persistenceService,
            ProviderSelectionManager providerSelectionManager) {
        this.projectScanner = projectScanner;
        this.complianceCheckerService = complianceCheckerService;
        this.obsolescenceDetectorService = obsolescenceDetectorService;
        this.persistenceService = persistenceService;
        this.providerSelectionManager = providerSelectionManager;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LOGGER.info("Starting dashboard radar audit batch");
        applyProviderSelection(chunkContext);
        List<ProjectSnapshot> projects;
        try {
            projects = projectScanner.fetchProjects();
        } finally {
            providerSelectionManager.clear();
        }
        LOGGER.info("Fetched {} projects from configured SCM providers", projects.size());
        for (ProjectSnapshot snapshot : projects) {
            LOGGER.info("Processing project {}", snapshot.fullName());
            ComplianceReport compliance = complianceCheckerService.checkCompliance(snapshot);
            ObsolescenceReport obsolescence = obsolescenceDetectorService.detect(snapshot);
            persistenceService.persist(new ProjectAudit(snapshot, compliance, obsolescence));
        }
        return RepeatStatus.FINISHED;
    }

    private void applyProviderSelection(ChunkContext chunkContext) {
        for (String key : new String[] {"providers", "scm-providers", "scmProviders"}) {
            Object providers = chunkContext.getStepContext().getJobParameters().get(key);
            if (providers instanceof String providerString && !providerString.isBlank()) {
                LOGGER.info("Overriding SCM providers for this execution with {}", providerString);
                providerSelectionManager.selectProviders(providerString);
                return;
            }
        }
    }
}
