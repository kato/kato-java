plugins {
    java
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":common"))

    compileOnly("org.springframework:spring-web")
    compileOnly("io.github.openfeign:feign-core")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("org.springframework.cloud:spring-cloud-openfeign-core")
}