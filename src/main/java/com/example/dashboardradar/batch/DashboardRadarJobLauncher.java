package com.example.dashboardradar.batch;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
        JobParametersBuilder parametersBuilder = new JobParametersBuilder()
                .addLong("timestamp", Instant.now().toEpochMilli());
        extractProviderSelection(args).ifPresent(providers -> {
            LOGGER.info("Launching job with SCM providers selection: {}", providers);
            parametersBuilder.addString("providers", providers);
        });
        JobParameters parameters = parametersBuilder.toJobParameters();
        jobLauncher.run(dashboardRadarJob, parameters);
    }

    private static Optional<String> extractProviderSelection(ApplicationArguments args) {
        if (args == null) {
            return Optional.empty();
        }
        for (String optionName : new String[] {"providers", "scm-providers", "scmProviders"}) {
            List<String> values = args.getOptionValues(optionName);
            if (values != null && !values.isEmpty()) {
                return Optional.ofNullable(values.get(values.size() - 1));
            }
        }
        return Optional.empty();
    }
}
