plugins {
    id("java")
    kotlin("jvm")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(project(":common"))

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.springframework:spring-context:5.3.19")
    compileOnly("org.springframework:spring-webmvc:5.3.19")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}