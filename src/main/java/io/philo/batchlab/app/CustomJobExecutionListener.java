package io.philo.batchlab.app;

import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameter;
import org.springframework.stereotype.Component;

@Slf4j
public class CustomJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {

        var jobParameters = jobExecution.getJobParameters();

        for (Entry<String, JobParameter<?>> entry : jobParameters.getParameters()
            .entrySet()) {
            String key = entry.getKey();
            JobParameter<?> jobParameter = entry.getValue();
            log.info("job key: " + key + ", value: " + jobParameter.getValue());
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        JobExecutionListener.super.afterJob(jobExecution);
    }
}
