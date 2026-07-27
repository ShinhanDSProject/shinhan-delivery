package com.example.shinhangaecheokja.common;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 프로젝트 레이어드 아키텍처 규칙 및 도메인 격리 컨벤션을 검증하는 ArchUnit 테스트 하네스입니다. (code-convention.md §14 참조) */
class ArchUnitTest {

  private static JavaClasses importedClasses;

  @BeforeAll
  static void setup() {
    importedClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.shinhangaecheokja");
  }

  @Test
  @DisplayName("Controller 계층은 Repository 계층을 직접 참조해서는 안 된다 (반드시 Service 거침)")
  void controller는_repository를_직접_호출하지_않는다() {
    noClasses()
        .that()
        .resideInAPackage("..controller..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..repository..")
        .check(importedClasses);
  }

  @Test
  @DisplayName("Repository 계층은 Service나 Controller 계층에 역방향 의존해서는 안 된다")
  void repository는_service나_controller에_의존하지_않는다() {
    noClasses()
        .that()
        .resideInAPackage("..repository..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..service..", "..controller..")
        .check(importedClasses);
  }

  @Test
  @DisplayName("도메인 계층 간(member, vehicle, delivery, payment) 타 도메인의 Repository를 직접 참조해서는 안 된다")
  void 타_도메인의_repository를_직접_참조하지_않는다() {
    noClasses()
        .that()
        .resideInAPackage("..vehicle..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..member.repository..")
        .check(importedClasses);

    noClasses()
        .that()
        .resideInAPackage("..delivery..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..member.repository..", "..vehicle.repository..")
        .check(importedClasses);

    noClasses()
        .that()
        .resideInAPackage("..payment..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..member.repository..", "..vehicle.repository..", "..delivery.repository..")
        .check(importedClasses);
  }
}
