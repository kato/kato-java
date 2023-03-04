import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.jetbrains.kotlin.konan.properties.hasProperty

plugins {
    id("org.springframework.boot") version "3.0.3" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "1.7.22" apply false
    kotlin("plugin.spring") version "1.7.22" apply false

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
val jvmVersion = "17"
val springBootVer = "3.0.3"
val springCloudVersion = "2022.0.1"

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
    version = "0.1.0."
    if (ext["development"] == true)
        version = "$version-SNAPSHOT"

    tasks.withType<JavaCompile> {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
        options.encoding = "UTF-8"
    }

    tasks.withType(Javadoc::class) {
        options.encoding = "UTF-8"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = jvmVersion
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    apply<io.spring.gradle.dependencymanagement.DependencyManagementPlugin>()
    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${springBootVer}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
        }
    }

    pluginManager.withPlugin("java") {
        extensions.configure(JavaPluginExtension::class) {
            withJavadocJar()
            withSourcesJar()
        }
    }

    val isSnapshot = version.toString().endsWith("SNAPSHOT")
    val isLanCiServer = (System.getenv("CI_RUNNER_TAGS") ?: "").contains("LAN")
    val ciUsername = System.getenv(extra["CI_UN_ENV_KEY"].toString()) ?: ""
    val ciPassword = System.getenv(extra["CI_PD_ENV_KEY"].toString()) ?: ""

    pluginManager.withPlugin("maven-publish") {
        extensions.configure(PublishingExtension::class) {
            publications {
                create<MavenPublication>("Java") {
                    repositories {
                        mavenCentral()
                        maven {
                            maven(
                                getMavenArtifactRepo(
                                    "http://repo.gate.bjknrt.com/repository/${if (isSnapshot) "maven-snapshot" else "maven-release"}/",
                                    "bjknrt",
                                    ciUsername,
                                    ciPassword
                                )
                            )
                        }
                    }
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
                    versionMapping {
                        usage("java-api") {
                            fromResolutionOf("runtimeClasspath")
                        }
                        usage("java-runtime") {
                            fromResolutionResult()
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

    repositories {
        val url = if (isLanCiServer) {
            "http://192.168.3.201:8081/repository/maven-public/"
        } else {
            "https://repo.gate.bjknrt.com/repository/maven-public/"
        }
        maven(getMavenArtifactRepo(url))
    }
}
fun getMavenArtifactRepo(
    url: String,
    repoName: String = "DEFAULT_REPO_NAME",
    _username: String = "bjknrt",
    _password: String = "bjknrt"
): (MavenArtifactRepository).() -> Unit {
    return {
        name = repoName
        isAllowInsecureProtocol = true
        setUrl(url)
        credentials {
            username = _username
            password = _password
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