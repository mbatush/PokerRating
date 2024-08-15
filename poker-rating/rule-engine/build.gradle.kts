plugins {
    id("spring.boot.convention")
    id("helm.convention")
}

version = "1.0.1"

dependencies {
    implementation(project(":poker-rating:model"))
    implementation(project(":poker-rating:util"))
    implementation(project(":poker-rating:player-rating-service"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-ui")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.zalando:problem-spring-web-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("com.andrebreves:java-tuple")
    implementation("com.google.guava:guava")

    testImplementation(project(":poker-rating:test-util"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.javacrumbs.json-unit:json-unit-spring")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
}
