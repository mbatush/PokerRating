plugins {
    id("java.convention")
    id("application")
}

application {
    mainClass.set("com.poker.test.RunBulk")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.junit.jupiter:junit-jupiter-api")
}
