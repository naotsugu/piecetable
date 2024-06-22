rootProject.name = "piecetable"
include("lib", "app", "dev")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}
