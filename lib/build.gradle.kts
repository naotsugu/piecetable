plugins {
    `java-library`
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

