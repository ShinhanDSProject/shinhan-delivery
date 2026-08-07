package com.example.shinhandelivery.common;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
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
  @DisplayName("모든 도메인 서비스 계층은 타 도메인의 Repository를 직접 참조하지 않고 해당 도메인의 Service를 거쳐야 한다 (동적 전수 검증).")
  void domainServiceShouldNotDependOnOtherDomainRepository() {
    classes()
        .that()
        .resideInAPackage("com.example.shinhandelivery..service..")
        .should(notDependOnRepositoryOfOtherDomain())
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

  private static ArchCondition<JavaClass> notDependOnRepositoryOfOtherDomain() {
    return new ArchCondition<>("not depend on repository of a different domain") {
      @Override
      public void check(JavaClass serviceClass, ConditionEvents events) {
        String serviceDomain = extractDomain(serviceClass.getPackageName());
        if (serviceDomain == null) {
          return;
        }

        for (Dependency dependency : serviceClass.getDirectDependenciesFromSelf()) {
          JavaClass targetClass = dependency.getTargetClass();
          String targetPackage = targetClass.getPackageName();
          String targetDomain = extractDomain(targetPackage);

          if (isRepositoryPackage(targetPackage)) {
            if (targetDomain != null && !targetDomain.equals(serviceDomain)) {
              String message =
                  String.format(
                      "서비스 '%s' (도메인: %s)가 타 도메인 Repository '%s' (도메인: %s)를 직접 참조하고 있습니다.",
                      serviceClass.getSimpleName(),
                      serviceDomain,
                      targetClass.getSimpleName(),
                      targetDomain);
              events.add(SimpleConditionEvent.violated(serviceClass, message));
            }
          }
        }
      }

      private boolean isRepositoryPackage(String packageName) {
        return packageName.contains(".repository.") || packageName.endsWith(".repository");
      }

      private String extractDomain(String packageName) {
        String prefix = "com.example.shinhandelivery.";
        if (!packageName.startsWith(prefix)) {
          return null;
        }
        String subPackage = packageName.substring(prefix.length());
        String[] parts = subPackage.split("\\.");
        return parts.length > 0 ? parts[0] : null;
      }
    };
  }
}
