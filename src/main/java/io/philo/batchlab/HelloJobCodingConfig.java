package io.philo.batchlab;

import java.time.LocalDateTime;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Configuration
public class HelloJobCodingConfig {

    private final JobRepository jobRepository;
    private final AbstractPlatformTransactionManager transactionManager;

    public HelloJobCodingConfig(JobRepository jobRepository,
                                AbstractPlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job helloWorldJob(Step helloWorldStep) {
        System.out.println(">>>> hello world spring batch job !!");
        return new JobBuilder("helloWorldJob", jobRepository)
            .start(helloWorldStep)
            .listener(new JobExecutionListener() {
                @Override
                public void beforeJob(JobExecution jobExecution) {
                    var param = jobExecution.getJobParameters().getLocalDateTime("requestDate");
                    System.out.println("Before job, param is: " + param.toString());
                }

                @Override
                public void afterJob(JobExecution jobExecution) {
                    System.out.println("After job");
                }
            })
            .build();
    }

    @JobScope
    @Bean
    public Step helloWorldStep(Tasklet helloWorldStepTasklet) {
        System.out.println(">>>> this is helloWorldStep !!");
        return new StepBuilder("helloWorldStep", jobRepository)
            .tasklet(helloWorldStepTasklet, transactionManager)
            .build();
    }

    @StepScope
    @Bean
    public Tasklet helloWorldStepTasklet(
        @Value("#{jobParameters['requestDate']}") LocalDateTime requestDateTime
    ) {
        System.out.println(">>>> hello world spring batch tasklet !!");
        System.out.println(">>>> input data, requestDateTime: " + requestDateTime);

        return (contribution, chunkContext) -> RepeatStatus.FINISHED;
    }
}

