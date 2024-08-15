plugins {
    id("java.convention")
}

dependencies {
    implementation(project(":poker-rating:model"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.slf4j:slf4j-api")
}
