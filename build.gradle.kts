import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.jetbrains.kotlin.konan.properties.hasProperty

plugins {
    id("org.springframework.boot") version "2.6.7" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.6.21" apply false
    kotlin("plugin.spring") version "1.6.21" apply false

    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val globalProperties = java.util.Properties()
if (System.getenv("RELEASE") != null) {
    globalProperties["release"] = true
    globalProperties["development"] = false
} else {
    globalProperties["release"] = false
    globalProperties["development"] = true
}
val globalLocalFile = project.file("local.properties")
if (globalLocalFile.isFile)
    globalProperties.load(globalLocalFile.inputStream())
globalProperties.forEach { key, value ->
    ext.set(key as String, value)
}

allprojects {
    //属性文件
    val localProperties = globalProperties.clone() as java.util.Properties
    val localFile = this.file("local.properties")
    if (localFile.isFile)
        localProperties.load(localFile.inputStream())
    localProperties.forEach { key, value ->
        ext.set(key as String, value)
    }

    group = "me.danwi.kato"
    version = "0.0.5"
    if (ext["development"] == true)
        version = "$version-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.encoding = "UTF-8"
    }

    tasks.withType(Javadoc::class) {
        options.encoding = "UTF-8"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    pluginManager.withPlugin("java") {
        extensions.configure(JavaPluginExtension::class) {
            withJavadocJar()
            withSourcesJar()
        }
    }

    pluginManager.withPlugin("maven-publish") {
        extensions.configure(PublishingExtension::class) {
            publications {
                create<MavenPublication>("Java") {
                    from(components["java"])
                    pom {
                        name.set(project.name)
                        description.set("kato ${project.name} component")
                        url.set("https://github.com/kato/kato-java")
                        scm {
                            connection.set("scm:git:https://github.com/kato/kato-java.git")
                            developerConnection.set("scm:git:https://github.com/kato/kato-java.git")
                            url.set("https://github.com/kato/kato-java")
                        }
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("danwi")
                                name.set("danwi")
                                email.set("demon@danwi.me")
                            }
                        }
                    }
                }
            }
        }

        if (localProperties.hasProperty("signing.key"))
            pluginManager.withPlugin("signing") {
                extensions.configure(SigningExtension::class) {
                    useInMemoryPgpKeys(
                        localProperties["signing.key"].toString(),
                        localProperties["signing.password"].toString()
                    )
                    extensions.configure(PublishingExtension::class) {
                        publications.forEach { sign(it) }
                    }
                }
            }
    }
}

if (globalProperties.hasProperty("ossrh.username")) {
    pluginManager.withPlugin("io.github.gradle-nexus.publish-plugin") {
        extensions.configure(NexusPublishExtension::class) {
            repositories {
                sonatype {
                    username.set(globalProperties["ossrh.username"].toString())
                    password.set(globalProperties["ossrh.password"].toString())
                }
            }
        }
    }
}