import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("java.convention")
    id("org.springframework.boot")
}

fun Project.findPropertyOrDefault(s: String, def: String): String =
    (findProperty(s) ?: def) as String

springBoot {
    buildInfo()
}

afterEvaluate {
    val registryUser = findPropertyOrDefault("registryUser", "N/A")
    val registryPassword = findPropertyOrDefault("registryPassword", "N/A")
    val dockerTag = if (project.hasProperty("release")) project.version as String else "latest"

    tasks {
        withType<BootBuildImage> {
            docker {
                imageName = "stef.jfrog.io/default-docker-local/${project.name}:${dockerTag}"
                if (project.hasProperty("publish")) {
                    isPublish = true
                }
                publishRegistry {
                    username = registryUser
                    password = registryPassword
                    url = "https://stef.jfrog.io"
                    email = registryUser
                }
            }
        }

        named("build") {
            dependsOn("bootBuildImage")
        }
    }
}

