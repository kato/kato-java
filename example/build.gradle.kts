plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(project(":client"))
    implementation(project(":server"))


    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.hibernate.validator:hibernate-validator")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.hibernate.validator:hibernate-validator")
}


