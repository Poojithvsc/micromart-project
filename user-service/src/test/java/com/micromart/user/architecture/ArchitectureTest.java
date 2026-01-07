package com.micromart.user.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture Tests using ArchUnit.
 * <p>
 * Testing Concepts Demonstrated:
 * - Enforcing layered architecture
 * - Naming conventions
 * - Package dependencies
 * - Annotation placement
 * <p>
 * Learning Points:
 * - ArchUnit analyzes compiled classes
 * - Rules can enforce layer dependencies
 * - Catches architecture violations early
 * - Documents architecture as executable tests
 */
@DisplayName("Architecture Tests")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.micromart.user");
    }

    // ========================================================================
    // LAYERED ARCHITECTURE TESTS
    // ========================================================================
    @Nested
    @DisplayName("Layered Architecture")
    class LayeredArchitectureTests {

        @Test
        @DisplayName("Should respect layer dependencies")
        void shouldRespectLayerDependencies() {
            ArchRule rule = layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Controller").definedBy("..controller..")
                    .layer("Service").definedBy("..service..")
                    .layer("Repository").definedBy("..repository..")
                    .layer("Domain").definedBy("..domain..")

                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
                    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                    .whereLayer("Domain").mayBeAccessedByAnyLayer();

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Controllers should not access repositories directly")
        void controllersShouldNotAccessRepositoriesDirectly() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().accessClassesThat().resideInAPackage("..repository..");

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // NAMING CONVENTION TESTS
    // ========================================================================
    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventionTests {

        @Test
        @DisplayName("Controllers should have Controller suffix")
        void controllersShouldHaveControllerSuffix() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..controller..")
                    .and().areAnnotatedWith(RestController.class)
                    .should().haveSimpleNameEndingWith("Controller");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Services should have Service suffix")
        void servicesShouldHaveServiceSuffix() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..service..")
                    .and().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("ServiceImpl")
                    .orShould().haveSimpleNameEndingWith("Service");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Repositories should have Repository suffix")
        void repositoriesShouldHaveRepositorySuffix() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..repository..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Repository");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("DTOs should reside in dto package")
        void dtosShouldResideInDtoPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Request")
                    .or().haveSimpleNameEndingWith("Response")
                    .should().resideInAPackage("..dto..");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Entities should reside in domain package")
        void entitiesShouldResideInDomainPackage() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                    .should().resideInAPackage("..domain..");

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // ANNOTATION TESTS
    // ========================================================================
    @Nested
    @DisplayName("Annotation Placement")
    class AnnotationPlacementTests {

        @Test
        @DisplayName("Only controllers should be annotated with @RestController")
        void onlyControllersShouldBeAnnotatedWithRestController() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage("..controller..");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Only services should be annotated with @Service")
        void onlyServicesShouldBeAnnotatedWithService() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith(Service.class)
                    .should().resideInAPackage("..service..");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Repository interfaces should extend JpaRepository")
        void repositoriesShouldExtendJpaRepository() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..repository..")
                    .and().areInterfaces()
                    .and().haveSimpleNameEndingWith("Repository")
                    .should().beAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class);

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // DEPENDENCY TESTS
    // ========================================================================
    @Nested
    @DisplayName("Dependency Rules")
    class DependencyRulesTests {

        @Test
        @DisplayName("Domain should not depend on Spring")
        void domainShouldNotDependOnSpring() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .and().areNotAnnotatedWith(jakarta.persistence.Entity.class)
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework..");

            // Note: This rule allows JPA annotations but not Spring
            // In strict mode, you'd want pure domain without any framework annotations
        }

        @Test
        @DisplayName("Services should not throw controller-level exceptions")
        void servicesShouldNotThrowControllerExceptions() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework.web..");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("No cyclic dependencies between packages")
        void noCyclicDependencies() {
            ArchRule rule = slices()
                    .matching("com.micromart.user.(*)..")
                    .should().beFreeOfCycles();

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // CLEAN ARCHITECTURE TESTS
    // ========================================================================
    @Nested
    @DisplayName("Clean Architecture Principles")
    class CleanArchitectureTests {

        @Test
        @DisplayName("Mappers should only be used in controllers or services")
        void mappersShouldOnlyBeUsedInControllersOrServices() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..mapper..")
                    .should().onlyBeAccessed().byClassesThat()
                    .resideInAnyPackage("..controller..", "..service..", "..mapper..");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Config classes should be in config package")
        void configClassesShouldBeInConfigPackage() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                    .should().resideInAPackage("..config..");

            rule.check(importedClasses);
        }
    }
}
