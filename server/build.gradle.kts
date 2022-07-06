plugins {
    java
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    api(project(":common"))

    compileOnly("org.springframework:spring-context:5.3.19")
    compileOnly("org.springframework:spring-webmvc:5.3.19")
    compileOnly("org.springframework.security:spring-security-core:5.6.3")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.7")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:9.0.62")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}