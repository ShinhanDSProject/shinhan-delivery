package com.example.shinhandelivery.common;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LayeredArchitectureTest {

  private JavaClasses importedClasses;

  @BeforeEach
  void setUp() {
    importedClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.shinhandelivery");
  }

  @Test
  @DisplayName("Controller 계층은 Repository 계층을 직접 참조하지 않고 Service 계층을 거쳐야 한다.")
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
  @DisplayName("Repository 계층은 Service나 Controller 계층을 참조하지 않아야 한다 (역방향 의존 금지).")
  void repositoryShouldNotDependOnUpperLayers() {
    noClasses()
        .that()
        .resideInAPackage("..repository..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..service..", "..controller..")
        .check(importedClasses);
  }

  @Test
  @DisplayName("도메인 서비스 계층은 다른 도메인의 Repository를 직접 참조하지 않고 해당 도메인의 Service를 거쳐야 한다.")
  void domainServiceShouldNotDependOnOtherDomainRepository() {
    noClasses()
        .that()
        .resideInAPackage("..delivery.service..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..member.repository..", "..vehicle.repository..", "..payment.repository..")
        .allowEmptyShould(true)
        .check(importedClasses);

    noClasses()
        .that()
        .resideInAPackage("..payment.service..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..member.repository..", "..vehicle.repository..", "..delivery.repository..")
        .allowEmptyShould(true)
        .check(importedClasses);

    noClasses()
        .that()
        .resideInAPackage("..vehicle.service..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..member.repository..", "..payment.repository..", "..delivery.repository..")
        .allowEmptyShould(true)
        .check(importedClasses);
  }

  @Test
  @DisplayName("DTO 및 Entity 계층은 상위 Service나 Controller 계층을 참조하지 않아야 한다.")
  void domainModelShouldNotDependOnUpperLayers() {
    noClasses()
        .that()
        .resideInAnyPackage("..dto..", "..entity..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..service..", "..controller..")
        .check(importedClasses);
  }
}
