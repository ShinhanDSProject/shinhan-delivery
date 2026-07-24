package com.example.shinhangaecheokja.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

// code-convention.md 14번: domain 레이어 의존성 규칙을 빌드 시점에 강제한다.
class LayeredArchitectureTest {

  private static JavaClasses importedClasses;

  @BeforeAll
  static void setup() {
    importedClasses = new ClassFileImporter().importPackages("com.example.shinhangaecheokja");
  }

  @Test
  void domain_레이어는_spring에_의존하지_않는다() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.springframework..")
        .allowEmptyShould(true)
        .check(importedClasses);
  }

  @Test
  void domain_레이어는_application_infrastructure_presentation에_의존하지_않는다() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..application..", "..infrastructure..", "..presentation..")
        .allowEmptyShould(true)
        .check(importedClasses);
  }
}
