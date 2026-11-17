import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val ver = Properties().apply { file("../version.properties").inputStream().use { load(it) } }

android {
    namespace = "com.example.varinbloknot"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.varinbloknot"
        minSdk = 26
        targetSdk = 36
        versionCode = ver.getProperty("VERSION_CODE").toInt()
        versionName = ver.getProperty("VERSION_NAME")
        ndk { abiFilters += "arm64-v8a" }
        buildConfigField("String", "REPO", "\"TimurNazmutdinov/VarinBloknot\"")
        buildConfigField("String", "BRANCH", "\"main\"")
    }

    signingConfigs {
        create("release") {
            val f = providers.gradleProperty("VARIN_STORE_FILE").orNull
            if (f != null) {
                storeFile = file(f)
                storePassword = providers.gradleProperty("VARIN_STORE_PASSWORD").get()
                keyAlias = providers.gradleProperty("VARIN_KEY_ALIAS").get()
                keyPassword = providers.gradleProperty("VARIN_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Встроенный контент (запасной) берём из папки content/ в корне репозитория
    sourceSets["main"].assets.srcDir("../content")
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.runtime:runtime:1.12.1")
    implementation("androidx.compose.ui:ui:1.12.1")
    implementation("androidx.compose.foundation:foundation:1.12.1")
    implementation("androidx.compose.animation:animation:1.12.1")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")
}
