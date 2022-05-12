plugins {
    java
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.github.openfeign:feign-core:11.8")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    compileOnly("org.springframework.cloud:spring-cloud-openfeign-core:3.1.2")
}