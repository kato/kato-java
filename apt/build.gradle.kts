plugins {
    java
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.github.chhorz:javadoc-parser:0.2.0")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}