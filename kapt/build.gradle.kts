plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

dependencies {
    api(project(":apt"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
}