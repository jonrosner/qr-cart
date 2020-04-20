plugins {
    id("com.android.library")
    id("kotlin-multiplatform")
    id("kotlin-android-extensions")
}

val vision_version: String by project
val androidx_base: String by project

android {
    compileSdkVersion(28)
    buildToolsVersion = "29.0.2"
    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(28)
    }
    buildTypes {
        val release by getting {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
kotlin {
    android()

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation("androidx.appcompat:appcompat:$androidx_base")
                implementation("com.google.android.gms:play-services-vision:$vision_version")
            }
        }
    }
}
