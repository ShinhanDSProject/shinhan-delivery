package com.example.shinhangaecheokja.common;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 데이터베이스 마이그레이션 수행 시, 기존 데이터가 존재할 때 발생할 수 있는 스키마 제약조건 오류를 검증합니다.
 *
 * <p>
 *
 * <h3>💡 마이그레이션 데이터 정합성 주의 사항:</h3>
 *
 * 데이터가 들어있는 실서버 환경에 새로운 컬럼(예: {@code NOT NULL})이나 제약조건(예: {@code UNIQUE})을 추가할 때, 기본값(Default)을
 * 누락하면 기존 레코드들과 충돌이 발생해 마이그레이션이 실패하게 됩니다. 이 테스트는 해당 시나리오를 가상 테스트 데이터와 함께 사전 재현하여 검사합니다.
 */
@SpringBootTest
@ActiveProfiles("test") // 테스트 전용 Profile 적용으로 실제 운영/로컬 DB 격리 안전장치 확보
class FlywayMigrationDataCompatibilityTest {

  @Autowired private DataSource dataSource;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("기존 데이터가 적재된 상태에서도 모든 Flyway 마이그레이션이 제약 조건 오류 없이 통과하는지 검증한다.")
  void testMigrationWithExistingData() {
    // 1. DB 클린업 및 V1 마이그레이션 수행
    Flyway flywayV1 =
        Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false) // clean 기능 명시적 활성화
            .target("2") // V2 스키마(Member 테이블 구축)를 타겟팅
            .load();
    flywayV1.clean(); // 기존 테스트 스키마 완전 초기화
    flywayV1.migrate(); // V2 Schema 적용

    // 2. V1 스키마 기준 테이블들에 더미 데이터 삽입 (기존 데이터 상태 재현)
    // ⚠️ 주의: V1__init_schema.sql 내의 테이블 구조가 변경(컬럼 삭제 등)되는 경우,
    // 아래 가상 INSERT 구문의 컬럼 규격도 일치하도록 반드시 동기화 수정해 주어야 합니다.
    jdbcTemplate.execute(
        "INSERT INTO member (email, password, name, phone_number, role) "
            + "VALUES ('migration_test@example.com', 'hashed_pass', 'Mig Test', '010-9999-9999', 'CUSTOMER')");

    // 3. 최신 버전 스크립트까지 순차적으로 마이그레이션(Migrate) 연쇄 구동
    Flyway flywayLatest =
        Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .target("latest") // 최신 버전까지 적용
            .load();

    // 이 과정에서 데이터가 존재하는 상태에서 제약조건 위배(예: Default 없는 컬럼 추가 등)가 발생하면 예외가 던져져 테스트가 실패합니다.
    flywayLatest.migrate();
  }
}
