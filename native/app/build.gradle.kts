plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.goreecloud.gallery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.goreecloud.gallery"
        minSdk = 29
        targetSdk = 36
        versionCode = 4
        versionName = "0.3.1-dev"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":android-adapter"))
    testImplementation(kotlin("test"))
}
