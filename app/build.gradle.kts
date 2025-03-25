import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

val propertiesFile = rootProject.file("secret.properties")
if (propertiesFile.exists()) {
    val properties = Properties()
    properties.load(propertiesFile.inputStream())

    println("secret.properties found")

    if (properties.containsKey("KEY_STORE_PATH")) {
        println("release signing config: ${properties.getProperty("KEY_STORE_PATH")}")
        project.extensions.extraProperties.set("RELEASE_STORE_FILE", properties.getProperty("KEY_STORE_PATH"))
        project.extensions.extraProperties.set("RELEASE_STORE_PASSWORD", properties.getProperty("KEY_STORE_PASSWORD"))
        project.extensions.extraProperties.set("RELEASE_KEY_ALIAS", properties.getProperty("KEY_ALIAS"))
        project.extensions.extraProperties.set("RELEASE_KEY_PASSWORD", properties.getProperty("KEY_PASSWORD"))
    }
}

android {
    namespace = "com.sy.wikitok"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sy.wikitok"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            println("release signing config: ${project.hasProperty("RELEASE_STORE_FILE")}")
            if (project.hasProperty("RELEASE_STORE_FILE")) {
                println("release signing config2: ${project.property("RELEASE_STORE_FILE") as String}")
                storeFile = file(project.property("RELEASE_STORE_FILE") as String)
                storePassword = project.property("RELEASE_STORE_PASSWORD") as String
                keyAlias = project.property("RELEASE_KEY_ALIAS") as String
                keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.splash)

    // room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // ktor
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.android)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.serialization.ktxJson)

    // lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Koin for Android
    implementation(libs.koin.androidx.compose)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}