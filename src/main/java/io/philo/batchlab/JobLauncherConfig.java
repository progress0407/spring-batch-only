package io.philo.batchlab;

import java.time.LocalDateTime;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobLauncherConfig {

    private final JobLauncher jobLauncher;
    private final Job exampleJob;

    public JobLauncherConfig(JobLauncher jobLauncher, Job exampleJob) {
        this.jobLauncher = jobLauncher;
        this.exampleJob = exampleJob;
    }

    @Bean
    public CommandLineRunner runJob() {
        return args -> {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("requestDate", LocalDateTime.now().plusDays(2))
                .toJobParameters();

            jobLauncher.run(exampleJob, jobParameters);
        };
    }

}
