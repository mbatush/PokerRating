plugins {
    id("com.kiwigrid.helm")
}

val helmArtifactoryUrl = "https://stef.jfrog.io/artifactory/default-helm/"
val helmVersion = "3.9.0"
val mainHelmResource = "src/main/helm"
val registryUser = findPropertyOrDefault("registryUser", "N/A")
val registryPassword = findPropertyOrDefault("registryPassword", "N/A")
val dockerTag = if (project.hasProperty("release")) project.version as String else "latest"

fun Project.findPropertyOrDefault(s: String, def: String): String =
    (findProperty(s) ?: def) as String

helm {
    version = helmVersion
    val helmArtifactory = repositories.create("default-helm") {
        url(helmArtifactoryUrl)
        user(registryUser)
        password(registryPassword)
        deployVia(com.kiwigrid.k8s.helm.HelmRepository.DeploymentSpec.HttpMethod.PUT)
            .to(helmArtifactoryUrl)
    }
    expansions.putAll(
        providers.provider {
            mapOf(
                "helmVersion" to project.version.toString(),
                "appVersion" to dockerTag,
                "appName" to project.name
            )
        }
    )

    deployTo(helmArtifactory)
}

afterEvaluate {
    tasks {
        helmRepoSync {
            onlyIf {
                (project.hasProperty("registryUser") &&
                        project.hasProperty("registryPassword"))
                        ||
                        project.hasProperty("publish")
            }
        }

        named("build") {
            if (project.hasProperty("publish")) {
                dependsOn("helmDeploy")
            }
        }
    }
}
