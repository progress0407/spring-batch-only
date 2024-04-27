package io.philo.batchlab;

import io.philo.batchlab.app.domain.PayRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobLauncherConfig {

    private final JobLauncher jobLauncher;
    private final Job exampleJob;

    public JobLauncherConfig(JobLauncher jobLauncher,
                             @Qualifier("chunkSampleJob") Job job) {
        this.jobLauncher = jobLauncher;
        this.exampleJob = job;
    }

//    @Bean
    public CommandLineRunner runJob() {
        return args -> {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("requestDate", LocalDateTime.now().plusDays(2))
                .toJobParameters();

            jobLauncher.run(exampleJob, jobParameters);
        };
    }
}
