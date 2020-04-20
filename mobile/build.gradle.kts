buildscript {
    val kotlin_version: String by extra
    val gradle_android_version: String by extra
    val shadow_version: String by extra

    repositories {
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://maven.fabric.io/public")

        mavenLocal()
        google()
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.android.tools.build:gradle:$gradle_android_version")
        classpath("com.github.jengelman.gradle.plugins:shadow:$shadow_version")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("io.fabric.tools:gradle:1.31.0") // Crashlytics plugin
    }
}

val local = java.util.Properties()
val localProperties: File = rootProject.file("local.properties")
if (localProperties.exists()) {
    localProperties.inputStream().use { local.load(it) }
}

val mavenUser: String by local
val mavenPwd: String by local

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://dl.bintray.com/kotlin/kotlinx")
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://dl.bintray.com/sargunster/maven")
        maven("https://dl.bintray.com/kotlin/squash")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://maven.fabric.io/public")

        google()
        jcenter()

        maven("https://maven.goma-cms.org/repository/nimmsta-frameworks-release/") {
            credentials {
                username = mavenUser
                password = mavenPwd
            }
            metadataSources {
                mavenPom()
                artifact()
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
