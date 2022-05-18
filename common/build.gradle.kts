plugins {
    java
    `maven-publish`
    signing
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}