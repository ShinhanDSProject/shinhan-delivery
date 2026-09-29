package com.example.shinhandelivery.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

/** 백그라운드 Flyway 마이그레이션 컴포넌트의 재시도·복구 동작을 검증한다. */
class BackgroundFlywayMigrationRunnerTest {

  private BackgroundFlywayMigrationRunner createRunner(
      DataSource dataSource, int maxAttempts, long retryIntervalSeconds) {
    BackgroundFlywayMigrationRunner runner = new BackgroundFlywayMigrationRunner(dataSource);
    ReflectionTestUtils.setField(runner, "maxAttempts", maxAttempts);
    ReflectionTestUtils.setField(runner, "retryIntervalSeconds", retryIntervalSeconds);
    return runner;
  }

  private DataSource h2DataSource(String databaseName) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;MODE=MySQL");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  @Test
  @DisplayName("DB 연결이 가능하면 백그라운드 마이그레이션이 스키마를 실제로 생성한다.")
  void migrateCreatesSchemaWhenDatabaseIsReachable() {
    DataSource dataSource = h2DataSource("background_migration_success");

    createRunner(dataSource, 1, 1L).migrateUntilSuccess();

    Integer memberTableCount =
        new JdbcTemplate(dataSource)
            .queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE UPPER(table_name) = 'MEMBER'",
                Integer.class);
    assertThat(memberTableCount).isEqualTo(1);
  }

  @Test
  @DisplayName("DB 연결에 실패해도 예외를 던지지 않아 애플리케이션이 종료되지 않는다.")
  void migrateDoesNotThrowWhenDatabaseIsUnreachable() {
    DriverManagerDataSource unreachable = new DriverManagerDataSource();
    unreachable.setDriverClassName("org.h2.Driver");
    unreachable.setUrl("jdbc:h2:tcp://127.0.0.1:1/nonexistent");
    unreachable.setUsername("sa");
    unreachable.setPassword("");

    BackgroundFlywayMigrationRunner runner = createRunner(unreachable, 2, 0L);

    assertThatCode(runner::migrateUntilSuccess).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("이미 마이그레이션된 DB에 다시 수행해도 예외 없이 멱등하게 동작한다.")
  void migrateIsIdempotent() {
    DataSource dataSource = h2DataSource("background_migration_idempotent");
    BackgroundFlywayMigrationRunner runner = createRunner(dataSource, 1, 1L);

    runner.migrateUntilSuccess();

    assertThatCode(runner::migrateUntilSuccess).doesNotThrowAnyException();
  }
}
