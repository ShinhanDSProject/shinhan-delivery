package com.example.shinhandelivery.common.config;

import java.time.Duration;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * DB가 아직 준비되지 않았더라도 애플리케이션 컨테이너가 먼저 기동되어야 하는 단일 컨테이너 배포에서, Flyway 마이그레이션을 기동 이후 백그라운드에서 재시도하며
 * 수행해 주는 컴포넌트입니다.
 *
 * <p>기본 구성(Flyway가 기동 시점에 직접 마이그레이션)에서는 DB 연결에 실패하면 애플리케이션이 기동되지 못하고 종료됩니다. DB 장애와 무관하게 API 서버가 계속
 * 떠 있어야 하는 환경에서는 아래처럼 기동 시점의 Flyway와 Hibernate 메타데이터 접근을 끄고 이 컴포넌트를 활성화합니다.
 *
 * <pre>
 * SPRING_FLYWAY_ENABLED=false
 * SPRING_JPA_HIBERNATE_DDL_AUTO=none
 * SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MariaDBDialect
 * JPA_JDBC_METADATA_ACCESS=false
 * BACKGROUND_MIGRATION_ENABLED=true
 * </pre>
 *
 * <p>application.yaml의 app.background-migration.enabled 속성이 true인 경우에만 빈으로 등록됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.background-migration.enabled", havingValue = "true")
public class BackgroundFlywayMigrationRunner implements ApplicationRunner {

  private static final String MIGRATION_LOCATION = "classpath:db/migration";
  private static final String WORKER_THREAD_NAME = "background-flyway-migration";

  private final DataSource dataSource;

  @Value("${app.background-migration.retry-interval-seconds:10}")
  private long retryIntervalSeconds;

  /** 0 이하이면 마이그레이션이 성공할 때까지 무제한으로 재시도합니다. */
  @Value("${app.background-migration.max-attempts:0}")
  private int maxAttempts;

  /** 기동 흐름을 막지 않도록 별도의 데몬 스레드에서 마이그레이션을 시작합니다. */
  @Override
  public void run(ApplicationArguments args) {
    Thread worker = new Thread(this::migrateUntilSuccess, WORKER_THREAD_NAME);
    worker.setDaemon(true);
    worker.start();
    log.info("백그라운드 Flyway 마이그레이션을 시작했습니다. DB가 준비되는 대로 스키마를 반영합니다.");
  }

  /**
   * DB 연결이 가능해질 때까지 재시도하며 Flyway 마이그레이션을 수행합니다. 실패해도 예외를 밖으로 던지지 않아 애플리케이션이 종료되지 않습니다.
   *
   * <p>테스트에서 스레드 없이 동기적으로 검증할 수 있도록 package-private으로 둡니다.
   */
  void migrateUntilSuccess() {
    int attempt = 0;
    while (true) {
      attempt++;
      try {
        int executed = migrate();
        log.info("백그라운드 Flyway 마이그레이션 완료 (시도 {}회, 적용 {}건)", attempt, executed);
        return;
      } catch (RuntimeException exception) {
        if (maxAttempts > 0 && attempt >= maxAttempts) {
          log.error(
              "백그라운드 Flyway 마이그레이션이 {}회 시도 후에도 실패했습니다. DB 연결 설정을 확인해 주세요.",
              attempt,
              exception);
          return;
        }
        log.warn(
            "백그라운드 Flyway 마이그레이션 실패 (시도 {}회). {}초 뒤 다시 시도합니다. 원인: {}",
            attempt,
            retryIntervalSeconds,
            exception.getMessage());
      }
      if (!sleepBeforeRetry()) {
        log.warn("백그라운드 Flyway 마이그레이션 스레드가 중단되어 재시도를 종료합니다.");
        return;
      }
    }
  }

  private int migrate() {
    return Flyway.configure()
        .dataSource(dataSource)
        .locations(MIGRATION_LOCATION)
        .baselineOnMigrate(true)
        .load()
        .migrate()
        .migrationsExecuted;
  }

  /** 재시도 간격만큼 대기하며, 스레드가 중단되면 false를 반환해 재시도를 멈추게 합니다. */
  private boolean sleepBeforeRetry() {
    try {
      Thread.sleep(Duration.ofSeconds(retryIntervalSeconds).toMillis());
      return true;
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
