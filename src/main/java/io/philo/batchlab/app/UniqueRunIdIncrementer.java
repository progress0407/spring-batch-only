package io.philo.batchlab.app;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

/**
 * 동일 JobParameter로 계속 실행될 수 있는 구조로 변경
 * <br>
 * 참고: <a href="https://jojoldu.tistory.com/487">...</a>
 */
public class UniqueRunIdIncrementer extends RunIdIncrementer {

    private static final String RUN_ID = "run.id";

    @Override
    public JobParameters getNext(JobParameters parameters) {

        var params = (parameters == null) ? new JobParameters() : parameters;

        return new JobParametersBuilder()
            .addLong(RUN_ID, params.getLong(RUN_ID, 0L) + 1)
            .toJobParameters();
    }
}
