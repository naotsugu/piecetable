plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.34.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
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
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

version = "0.5.14"
group = "com.mammb"
base.archivesName.set("piecetable")

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to base.archivesName,
            "Implementation-Version" to project.version))
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), base.archivesName.get(), version.toString())
    configure(com.vanniktech.maven.publish.JavaLibrary(
        javadocJar = com.vanniktech.maven.publish.JavadocJar.Javadoc(),
        sourcesJar = true,
    ))
    pom {
        name.set("piecetable")
        description.set("Java implementation of PieceTable data structure.")
        inceptionYear.set("2024")
        url.set("https://github.com/naotsugu/piecetable")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("naotsugu")
                name.set("Naotsugu Kobayashi")
                url.set("https://github.com/naotsugu/")
            }
        }
        scm {
            url.set("https://github.com/naotsugu/piecetable/")
            connection.set("scm:git:git://github.com/naotsugu/piecetable.git")
            developerConnection.set("scm:git:ssh://git@github.com/naotsugu/piecetable.git")
        }
    }
}
