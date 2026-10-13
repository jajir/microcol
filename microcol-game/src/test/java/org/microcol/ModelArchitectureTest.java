package org.microcol;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class ModelArchitectureTest {

    @Test
    void modelIsIndependentOfOtherGamePackagesAndJavaFx() {
        noClasses().that().resideInAPackage("org.microcol.model..").should()
                .dependOnClassesThat(resideInAPackage("org.microcol..")
                        .and(resideOutsideOfPackage("org.microcol.model.."))
                        .or(resideInAPackage("javafx..")))
                .because(
                        "UI and AI depend on the shared model, never the reverse")
                .allowEmptyShould(false)
                .check(new ClassFileImporter()
                        .withImportOption(
                                ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("org.microcol.model"));
    }
}
