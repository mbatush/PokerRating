plugins {
    id("java.convention")
}

dependencies {
    implementation(project(":poker-rating:model"))
    implementation(project(":poker-rating:util"))

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("dev.failsafe:failsafe")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    testImplementation(project(":poker-rating:test-util"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.javacrumbs.json-unit:json-unit-spring")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
}
