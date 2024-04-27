package io.philo.batchlab;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P6SpySqlFormatter implements MessageFormattingStrategy {

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    @Override
    public String formatMessage(
        int connectionId,
        String now,
        long elapsed,
        String category,
        String prepared,
        String sql,
        String url
    ) {
        String formattedSql = formatSql(category, sql);
        return formatLog(elapsed, category, formattedSql);
    }

    private String formatSql(String category, String sql) {
        if (StringUtils.isNotBlank(sql) && isStatement(category)) {
            String trimmedSQL = trim(sql);
            if (isDdl(trimmedSQL)) {
                return FormatStyle.DDL.getFormatter().format(sql);
            } else {
                return FormatStyle.BASIC.getFormatter().format(sql);
            }
        }
        return sql;
    }

    private String formatLog(long elapsed, String category, String formattedSql) {
        return String.format("[%s] | %d ms | %s", category, elapsed, formattedSql);
    }

    private boolean isDdl(String trimmedSQL) {
        return trimmedSQL.startsWith("create") ||
            trimmedSQL.startsWith("alter") ||
            trimmedSQL.startsWith("comment");
    }

    private String trim(String sql) {
        return sql.trim().toLowerCase();
    }

    private boolean isStatement(String category) {
        return Category.STATEMENT.getName().equals(category);
    }
}
