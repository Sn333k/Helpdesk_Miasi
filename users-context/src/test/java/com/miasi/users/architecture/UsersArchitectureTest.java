package com.miasi.users.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

class UsersArchitectureTest {

  private static final JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("com.miasi.users");

  // ─── Domain isolation ─────────────────────────────────────────────────────

  @Test
  void domainShouldNotDependOnInfrastructure() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .check(CLASSES);
  }

  @Test
  void domainShouldNotDependOnApplicationServices() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..application.services..")
        .check(CLASSES);
  }

  @Test
  void domainShouldNotDependOnJakartaPersistence() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("jakarta..")
        .check(CLASSES);
  }

  @Test
  void domainShouldNotDependOnJavalin() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("io.javalin..")
        .check(CLASSES);
  }

  @Test
  void domainShouldNotDependOnHibernate() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.hibernate..")
        .check(CLASSES);
  }

  // ─── Port isolation ───────────────────────────────────────────────────────

  @Test
  void portsShouldNotDependOnInfrastructure() {
    noClasses()
        .that()
        .resideInAPackage("..application.ports..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .check(CLASSES);
  }

  // ─── Application-service isolation ────────────────────────────────────────

  @Test
  void applicationServicesShouldNotDependOnInfrastructure() {
    noClasses()
        .that()
        .resideInAPackage("..application.services..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .check(CLASSES);
  }

  // ─── Direction check ──────────────────────────────────────────────────────

  @Test
  void applicationServicesMustImplementAtLeastOneInboundPort() {
    ArchCondition<JavaClass> implementsInboundPort =
        new ArchCondition<JavaClass>("implement at least one interface from ports.inbound") {
          @Override
          public void check(JavaClass item, ConditionEvents events) {
            boolean implementsPort =
                item.getAllRawInterfaces().stream()
                    .anyMatch(i -> i.getPackageName().contains(".ports.inbound"));
            if (!implementsPort) {
              events.add(
                  SimpleConditionEvent.violated(
                      item, item.getName() + " does not implement any inbound port interface"));
            }
          }
        };

    classes()
        .that()
        .resideInAPackage("..application.services..")
        .and()
        .areNotInterfaces()
        .should(implementsInboundPort)
        .check(CLASSES);
  }
}
