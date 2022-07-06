plugins {
    java
    `maven-publish`
    signing
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")

    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.7")
    compileOnly("org.springframework:spring-context:5.3.19")
    compileOnly("org.springframework:spring-webmvc:5.3.19")
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:9.0.62")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}