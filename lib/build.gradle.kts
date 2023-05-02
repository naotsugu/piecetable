plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

version = "0.2.0"
base.archivesName.set("piecetable")

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to base.archivesName,
            "Implementation-Version" to project.version))
    }
}


publishing {
    publications {
        register<MavenPublication>("gpr") {
            artifactId = "piecetable"
            from(components["java"])
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
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/naotsugu/piecetable")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

