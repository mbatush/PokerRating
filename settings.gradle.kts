pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "poker-rating"

include(":poker-holdem-calculator-rest")
include(":poker-rating:model")
include(":poker-rating:util")
include(":poker-rating:test-util")
include(":poker-rating:rule-engine")
include(":poker-rating:rest")
include(":poker-rating:simulator-ui")
include(":poker-rating:player-rating-service")
