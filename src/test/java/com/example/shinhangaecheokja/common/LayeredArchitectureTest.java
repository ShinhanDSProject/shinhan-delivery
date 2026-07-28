package com.example.shinhangaecheokja.common;

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
            .importPackages("com.example.shinhangaecheokja");
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
}
