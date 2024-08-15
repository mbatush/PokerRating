plugins {
    id("io.spring.dependency-management")
    id("com.github.ben-manes.versions")
}

val springBootVersion = "2.7.2"
val springCloudVersion = "2021.0.3"
val springDocVersion = "1.6.9"
val testcontainersVersion = "1.17.3"


// https://github.com/spring-gradle-plugins/dependency-management-plugin/issues/56
afterEvaluate {
    dependencyManagement {
        dependencies {
            dependency("org.projectlombok:lombok-mapstruct-binding:0.2.0")
            dependency("org.mapstruct:mapstruct-processor:1.5.2.Final")
            dependency("com.andrebreves:java-tuple:1.2.0")
            dependency("com.uber.nullaway:nullaway:0.9.8")
            dependency("com.google.code.findbugs:jsr305:3.0.2")
            dependency("com.google.errorprone:error_prone_core:2.14.0")
            dependency("com.google.errorprone:javac:9+181-r4173-1")
            dependency("com.google.guava:guava:31.1-jre")
            dependency("dev.failsafe:failsafe:3.2.4")
            dependency("org.springdoc:springdoc-openapi-ui:$springDocVersion")
            dependency("org.springdoc:springdoc-openapi-webmvc-core:$springDocVersion")
            dependency("org.springdoc:springdoc-openapi-webflux-ui:$springDocVersion")
            dependency("org.springdoc:springdoc-openapi-security:$springDocVersion")
            dependency("org.springdoc:springdoc-openapi-native:$springDocVersion")
            dependency("org.springdoc:springdoc-openapi-data-rest:$springDocVersion")
            dependency("org.springdoc:springdoc-openapi-hateoas:$springDocVersion")
            dependency("org.zalando:problem-spring-web-starter:0.28.0-RC.0")
            dependency("net.javacrumbs.json-unit:json-unit-spring:2.35.0")
        }
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
            mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
        }
    }
}
