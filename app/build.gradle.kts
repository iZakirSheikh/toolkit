plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.prime.toolkit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prime.toolkit"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "3.0.0-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xwhen-guards",
            "-Xnon-local-break-continue"
        )
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(project(":theme"))
    implementation(project(":foundation"))
    implementation(project(":preferences"))
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    implementation(libs.chrisbanes.haze)
    implementation("androidx.compose.material3:material3:1.4.0-alpha15")
}