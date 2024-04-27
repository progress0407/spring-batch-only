package io.philo.batchlab.app;

import io.philo.batchlab.app.domain.Pay;
import io.philo.batchlab.app.domain.PayRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChunkSampleConfig {

    private static final int CHUNK_SIZE = 5;

    private final JobRepository jobRepository;
    private final AbstractPlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final PayRepository payRepository;

    @PostConstruct
    public void initData() {

        payRepository.deleteAll();

        var now = LocalDateTime.now();
        for (int i = 0; i < 15; i++) {
            payRepository.save(new Pay(10_000L, "some-tx", now));
        }
    }

    @Bean
    public Job chunkSampleJob() {
        return new JobBuilder("chunkSampleJob", jobRepository)
            .start(chunkSampleStep())
            .listener(jobExecutionListener())
            .incrementer(new UniqueRunIdIncrementer())
            .build();
    }

    @Bean
    public Step chunkSampleStep() {
        return new StepBuilder("chunkSampleStep", jobRepository)
            .<Pay, Pay>chunk(CHUNK_SIZE, transactionManager)
            .reader(jdbcPagingItemReader())
            .writer(jdbcPagingItemWriter())
            .listener(chunkListener())
            .build();
    }

    @Bean
    public JdbcPagingItemReader<Pay> jdbcPagingItemReader() {

        log.info("[READER] START");

        return new JdbcPagingItemReaderBuilder<Pay>()
            .pageSize(CHUNK_SIZE)
            .fetchSize(CHUNK_SIZE)
            .dataSource(dataSource)
            .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
            .queryProvider(createQueryProvider())
            .name("jdbcPagingItemReader")
            .parameterValues(Map.of("amount", 10_000L))
            .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() {

        var queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("id, amount, tx_name, tx_date_time");
        queryProvider.setFromClause("from pay");
        queryProvider.setWhereClause("where amount >= :amount");
        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));

        try {
            return queryProvider.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ItemWriter<Pay> jdbcPagingItemWriter() {

        log.info("[WRITER] START");

        return list -> {
            for (Pay pay: list) {
                log.info("[Item Writer One thing] Current Pay={}", pay);
            }
        };
    }

    private static JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {

                var jobParameters = jobExecution.getJobParameters();
                for (Entry<String, JobParameter<?>> entry : jobParameters.getParameters()
                    .entrySet()) {
                    String key = entry.getKey();
                    JobParameter<?> jobParameter = entry.getValue();
                    System.out.println("job key: " + key + ", value: " + jobParameter.getValue());
                }
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                System.out.println("After job");
            }
        };
    }

    private static ChunkListener chunkListener() {


        return new ChunkListener() {

            @Override
            public void beforeChunk(ChunkContext context) {
                log.info("chunk start! ");
            }
        };
    }
}
