plugins {
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

version = "0.5.0"
group = "com.mammb"
base.archivesName.set("piecetable")

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to base.archivesName,
            "Implementation-Version" to project.version))
    }
}

val ossrhToken: String? by project
val ossrhTokenPassword: String? by project

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "piecetable"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("piecetable")
                description.set("Java implementation of PieceTable data structure.")
                url.set("https://github.com/naotsugu/piecetable")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("naotsugu")
                        name.set("Naotsugu Kobayashi")
                        email.set("naotsugukobayashi@gmail.com")
                    }
                }
                scm {
                    connection.set("git@github.com:naotsugu/piecetable.git")
                    developerConnection.set("git@github.com:naotsugu/piecetable.git")
                    url.set("https://github.com/naotsugu/piecetable")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = ossrhToken
                password = ossrhTokenPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
