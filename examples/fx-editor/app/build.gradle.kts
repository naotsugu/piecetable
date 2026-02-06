plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.gradlex.extra-java-module-info") version "1.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("../../../lib/build/libs/piecetable-0.6.3.jar"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    mainClass = "com.mammb.code.editor.Main"
    mainModule = "code.editor"
    if (providers.systemProperty("debug").isPresent) {
        applicationDefaultJvmArgs = applicationDefaultJvmArgs.plus(listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "22"
    modules("javafx.controls")
}

extraJavaModuleInfo {
    module("org.openjfx:javafx-graphics", "javafx.graphics") {
        patchRealModule()
        requires("java.desktop")
        requires("java.xml")
        requires("jdk.unsupported")

        requiresTransitive("javafx.base")

        exports("javafx.animation")
        exports("javafx.application")
        exports("javafx.concurrent")
        exports("javafx.css")
        exports("javafx.css.converter")
        exports("javafx.geometry")
        exports("javafx.print")
        exports("javafx.scene")
        exports("javafx.scene.canvas")
        exports("javafx.scene.effect")
        exports("javafx.scene.image")
        exports("javafx.scene.input")
        exports("javafx.scene.layout")
        exports("javafx.scene.paint")
        exports("javafx.scene.robot")
        exports("javafx.scene.shape")
        exports("javafx.scene.text")
        exports("javafx.scene.transform")
        exports("javafx.stage")

        exports("com.sun.glass.ui",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.glass.utils",
            "javafx.media",
            "javafx.web")
        exports("com.sun.javafx.application",
            "java.base",
            "javafx.controls",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.css",
            "javafx.controls")
        exports("com.sun.javafx.cursor",
            "javafx.swing")
        exports("com.sun.javafx.embed",
            "javafx.swing")
        exports("com.sun.javafx.font",
            "javafx.web",
            "code.editor")
        exports("com.sun.javafx.geom",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web",
            "code.editor")
        exports("com.sun.javafx.geom.transform",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web",
            "code.editor")
        exports("com.sun.javafx.iio",
            "javafx.web")
        exports("com.sun.javafx.menu",
            "javafx.controls")
        exports("com.sun.javafx.scene",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.scene.input",
            "javafx.controls",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.scene.layout",
            "javafx.controls",
            "javafx.web")
        exports("com.sun.javafx.scene.text",
            "javafx.controls",
            "javafx.web",
            "code.editor")
        exports("com.sun.javafx.scene.traversal",
            "javafx.controls",
            "javafx.web")
        exports("com.sun.javafx.sg.prism",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.stage",
            "javafx.controls",
            "javafx.swing")
        exports("com.sun.javafx.text",
            "javafx.web")
        exports("com.sun.javafx.tk",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web",
            "code.editor")
        exports("com.sun.javafx.util",
            "javafx.controls",
            "javafx.fxml",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.prism",
            "javafx.media",
            "javafx.web")
        exports("com.sun.prism.image",
            "javafx.web")
        exports("com.sun.prism.paint",
            "javafx.web")
        exports("com.sun.scenario.effect",
            "javafx.web")
        exports("com.sun.scenario.effect.impl",
            "javafx.web")
        exports("com.sun.scenario.effect.impl.prism",
            "javafx.web")
    }
}

