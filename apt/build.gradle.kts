plugins {
    java
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    api("com.github.chhorz:javadoc-parser:0.2.0")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}