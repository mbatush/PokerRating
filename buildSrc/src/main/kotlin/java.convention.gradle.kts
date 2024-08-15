import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    id("dependency.convention")
    id("java")
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok-mapstruct-binding")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding")
    annotationProcessor("org.mapstruct:mapstruct-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // nullaway
    compileOnly("com.google.code.findbugs:jsr305")
    errorprone("com.uber.nullaway:nullaway")
    errorprone("com.google.errorprone:error_prone_core")
    errorproneJavac("com.google.errorprone:javac")

    testCompileOnly("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok-mapstruct-binding")
    testAnnotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

}

nullaway {
    annotatedPackages.addAll("com.poker")
}

tasks {
    withType<Test> {
        useJUnitPlatform {
            if (!project.hasProperty("it.enable")) {
                excludeTags("integration");
            }
        }
        testLogging {
            events(PASSED, SKIPPED, FAILED)
            exceptionFormat = FULL
            showExceptions = true
            showStackTraces = true
            showCauses = true
        }
    }
    withType<JavaCompile>().configureEach {
        if (!name.toLowerCase().contains("test")) {
            options.errorprone.nullaway {
                severity.set(CheckSeverity.ERROR)
            }
        }
    }
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat("1.14.0")
    }
    format("misc") {
        // not using "**/..." to help keep spotless fast
        target(
            ".gitignore",
            ".gitattributes",
            ".gitconfig",
            ".editorconfig",
            "*.md",
            "src/**/*.md",
            "docs/**/*.md",
            "*.sh",
            "src/**/*.properties"
        )
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
