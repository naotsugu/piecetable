plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation(project(mapOf("path" to ":lib")))
    implementation(project(":lib"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

application {
    mainClass.set("com.mammb.code.editor3.App")
    mainModule.set("com.mammb.code.editor")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.compileJava.configure { options.encoding = Charsets.UTF_8.name() }
tasks.compileTestJava.configure { options.encoding = Charsets.UTF_8.name() }

javafx {
    version = "20"
    modules("javafx.controls")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}
