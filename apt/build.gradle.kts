plugins {
    java
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    api(project(":common"))

    api("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    api("com.github.chhorz:javadoc-parser:0.2.0")
}