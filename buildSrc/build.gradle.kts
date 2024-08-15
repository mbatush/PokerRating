plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.7.2")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.8.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.2")
    implementation("net.ltgt.gradle:gradle-nullaway-plugin:1.3.0")
    implementation("com.palantir.gradle.docker:gradle-docker:0.34.0")
    implementation("com.kiwigrid:gradle-helm-plugin:1.7.0")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
}
