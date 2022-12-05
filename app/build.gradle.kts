plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

application {
    mainClass.set("com.mammb.code.editor.App")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.compileJava.configure { options.encoding = Charsets.UTF_8.name() }
tasks.compileTestJava.configure { options.encoding = Charsets.UTF_8.name() }

javafx {
    version = "19"
    modules("javafx.controls")
}
