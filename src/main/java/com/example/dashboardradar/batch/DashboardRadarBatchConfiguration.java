package com.example.dashboardradar.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class DashboardRadarBatchConfiguration {

    @Bean
    public Job dashboardRadarJob(JobRepository jobRepository, Step auditStep) {
        return new JobBuilder("dashboardRadarJob", jobRepository)
                .start(auditStep)
                .build();
    }

    @Bean
    public Step auditStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            AuditTasklet auditTasklet) {
        return new StepBuilder("auditStep", jobRepository)
                .tasklet(auditTasklet, transactionManager)
                .build();
    }
}
