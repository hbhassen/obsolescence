package com.example.dashboardradar.batch;

import com.example.dashboardradar.model.ComplianceReport;
import com.example.dashboardradar.model.ObsolescenceReport;
import com.example.dashboardradar.model.ProjectAudit;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.service.ComplianceCheckerService;
import com.example.dashboardradar.service.GithubScannerService;
import com.example.dashboardradar.service.ObsolescenceDetectorService;
import com.example.dashboardradar.service.PersistenceService;
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

    private final GithubScannerService githubScannerService;
    private final ComplianceCheckerService complianceCheckerService;
    private final ObsolescenceDetectorService obsolescenceDetectorService;
    private final PersistenceService persistenceService;

    public AuditTasklet(GithubScannerService githubScannerService,
            ComplianceCheckerService complianceCheckerService,
            ObsolescenceDetectorService obsolescenceDetectorService,
            PersistenceService persistenceService) {
        this.githubScannerService = githubScannerService;
        this.complianceCheckerService = complianceCheckerService;
        this.obsolescenceDetectorService = obsolescenceDetectorService;
        this.persistenceService = persistenceService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LOGGER.info("Starting dashboard radar audit batch");
        List<ProjectSnapshot> projects = githubScannerService.fetchProjects();
        LOGGER.info("Fetched {} projects from GitHub", projects.size());
        for (ProjectSnapshot snapshot : projects) {
            LOGGER.info("Processing project {}", snapshot.fullName());
            ComplianceReport compliance = complianceCheckerService.checkCompliance(snapshot);
            ObsolescenceReport obsolescence = obsolescenceDetectorService.detect(snapshot);
            persistenceService.persist(new ProjectAudit(snapshot, compliance, obsolescence));
        }
        return RepeatStatus.FINISHED;
    }
}
