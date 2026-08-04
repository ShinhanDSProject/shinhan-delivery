package com.example.shinhandelivery.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * JPA Entity와 Flyway 물리 데이터베이스 스키마 간의 정합성을 검증하는 테스트입니다.
 *
 * <p>
 *
 * <h3>💡 트러블슈팅 가이드 (이 테스트가 실패했을 때):</h3>
 *
 * 이 테스트는 스프링 부트 컨텍스트 로딩 시 Hibernate의 {@code ddl-auto: validate} 옵션을 활성화하여 기동합니다. 만약 테스트가
 * 실패(ApplicationContext 로드 실패)한다면 아래 원인들을 점검해 보세요:
 *
 * <ol>
 *   <li><b>JPA 엔티티와 SQL 컬럼명 불일치:</b> Entity 클래스의 {@code @Column(name = "...")} 설정 값이 Flyway SQL
 *       스크립트 상의 실제 컬럼명과 다를 때
 *   <li><b>Null 허용 여부(Nullable) 불일치:</b> Entity의 {@code nullable = false}와 SQL의 {@code NOT NULL}
 *       규격이 매핑되지 않을 때
 *   <li><b>타입 불일치:</b> Java의 필드 타입(예: String, Long, Double)과 SQL의 컬럼 데이터 타입(예: VARCHAR, BIGINT,
 *       DOUBLE) 크기나 규격이 호환되지 않을 때
 *   <li><b>테이블 유실:</b> 새로운 JPA Entity 클래스를 추가했으나 Flyway SQL 스크립트 작성 및 반영을 누락했을 때
 * </ol>
 *
 * 해결 방법: 실패 로그의 'Schema-validation: ...' 부분을 참고하여 자바 엔티티 또는 Flyway 마이그레이션 SQL의 설정을 동일하게 교정해 주세요.
 */
@SpringBootTest(
    properties = {
      "spring.jpa.properties.hibernate.ddl-auto=validate",
      "spring.flyway.enabled=true"
    })
class DatabaseSchemaValidationTest {

  @Test
  @DisplayName("JPA 엔티티 명세와 데이터베이스(Flyway) 물리 스키마 구조가 완전히 일치하는지 검증한다.")
  void validateJpaSchemaWithPhysicalDatabase() {
    // 이 테스트는 Spring ApplicationContext가 ddl-auto=validate 환경에서 성공적으로 기동되면 자동으로 통과합니다.
  }
}
