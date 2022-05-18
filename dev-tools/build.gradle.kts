plugins {
    java
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":common"))
    compileOnly(project(":server"))

    compileOnly("org.springframework:spring-context:5.3.19")
    compileOnly("org.springframework:spring-webmvc:5.3.19")
    compileOnly("org.springframework.security:spring-security-core:5.6.3")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.7")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
}