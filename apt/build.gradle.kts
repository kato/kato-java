plugins {
    java
    `java-library`
}

dependencies {
    api(project(":common"))

    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.github.chhorz:javadoc-parser:0.2.0")
}