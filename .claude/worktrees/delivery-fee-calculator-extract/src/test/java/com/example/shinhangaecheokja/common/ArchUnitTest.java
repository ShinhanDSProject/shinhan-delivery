package com.example.shinhandelivery.common;

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
            .importPackages("com.example.shinhandelivery");
  }

  @Test
  @DisplayName("Controller 계층은 Repository 계층을 직접 참조해서는 안 된다 (반드시 Service 거침)")
  void controllerShouldNotDependOnRepository() {
    noClasses()
        .that()
        .resideInAPackage("..controller..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..repository..")
        .check(importedClasses);
  }

  @Test
  @DisplayName("Repository는 Service나 Controller에 의존하지 않는다")
  void repositoryShouldNotDependOnServiceOrController() {
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
  void domainShouldNotDependOnOtherDomainRepository() {
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
