package com.example.dashboardradar.batch;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DashboardRadarJobLauncher implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardRadarJobLauncher.class);

    private final JobLauncher jobLauncher;
    private final Job dashboardRadarJob;

    public DashboardRadarJobLauncher(JobLauncher jobLauncher, Job dashboardRadarJob) {
        this.jobLauncher = jobLauncher;
        this.dashboardRadarJob = dashboardRadarJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Launching dashboard radar batch job");
        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", Instant.now().toEpochMilli())
                .toJobParameters();
        jobLauncher.run(dashboardRadarJob, parameters);
    }
}
