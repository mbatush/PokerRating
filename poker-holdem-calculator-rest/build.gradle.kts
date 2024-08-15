plugins {
    id("docker.convention")
    id("helm.convention")
}

version = "1.0.0"

val dockerTag = if (project.hasProperty("release")) project.version as String else "latest"

docker {
    name = "stef.jfrog.io/default-docker-local/${project.name}:${dockerTag}"
    tag("stefJfrog", "stef.jfrog.io/default-docker-local/${project.name}:${dockerTag}")
    setDockerfile(file("Dockerfile"))
    files(listOf("setup.py", "requirements.txt", "start-server.sh"))
    pull(true)
    noCache(true)
}

tasks {
    register<Copy>("copySrc") {
        logger.debug("copySrc")
        mustRunAfter("dockerPrepare")
        from(layout.projectDirectory) {
            include("src/**")
            exclude("**/main/helm/**")
        }.source.forEach {
            logger.debug("Source to be copied: $it")
        }
        into(layout.buildDirectory.dir("docker"))
    }

    named("docker") {
        dependsOn("copySrc")
    }

    named("build") {
        dependsOn("docker")
        if (project.hasProperty("publish")) {
            dependsOn("dockerPushStefJfrog")
        }
    }
}
