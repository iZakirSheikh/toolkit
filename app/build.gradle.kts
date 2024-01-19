plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.prime.toolkit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.prime.toolkit"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-test"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {

    implementation(libs.compose.activity)

    implementation(libs.core.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.preview)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.compose.material3)
    implementation(libs.coil)
    // FIXME: Tests are disabled for now.
    //  Why?
    //  Because my PC/laptop is old and slow and it can't handle that many dependencies; this makes
    //  it slow.
    implementation(project(path = ":core-ktx"))
    implementation(project(path = ":material2"))
    implementation(project(path = ":preferences"))
    implementation(project(path = ":material3"))
}