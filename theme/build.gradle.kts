plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id(libs.plugins.maven.publish.get().pluginId)
}

android {
    namespace = "com.zs.compose.theme"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":foundation"))
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material.ripple)
    implementation(libs.androidx.window)
}

// Because the components are created only during the afterEvaluate phase, you must
// configure your publications using the afterEvaluate() lifecycle method.
afterEvaluate {
    publishing {
        publications {
            // Create a Maven publication named "release"
            create<MavenPublication>("release") {
                // Use the release build variant component
                from(components["release"])
                // Customize publication attributes
                groupId = "com.zs.compose"
                artifactId = "theme"
                version = "3.0.0-dev01"
            }
        }
    }
}