// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
// 📦 Core plugins required for Android Library + Compose + Maven publishing.
plugins {
    alias(libs.plugins.android.library)       // Android library plugin
    alias(libs.plugins.compose.compiler)      // Jetpack Compose compiler plugin
    id(libs.plugins.maven.publish.get().pluginId) // Maven publishing plugin
}


// -----------------------------------------------------------------------------
// ANDROID CONFIGURATION
// -----------------------------------------------------------------------------
// 🎨 Android library setup for Compose theme utilities.
android {
    namespace = "com.zs.compose.theme"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro") // ProGuard rules for consumers
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

    // Java 17 compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Enable Jetpack Compose
    buildFeatures { compose = true }

    // Configure publishing artifacts (sources + Javadoc)
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}


// -----------------------------------------------------------------------------
// DEPENDENCIES
// -----------------------------------------------------------------------------
// 📚 Core libraries: Compose foundation, ripple effects, window APIs, and project modules.
dependencies {
    implementation(platform(libs.compose.bom))        // Compose BOM for version alignment
    implementation(project(":foundation"))            // Internal foundation module
    implementation(libs.androidx.foundation)          // Compose foundation components
    implementation(libs.androidx.material.ripple)     // Ripple effect for touch feedback
    implementation(libs.androidx.window)              // WindowManager APIs (foldables, multi-window)
}


// -----------------------------------------------------------------------------
// PUBLISHING CONFIGURATION
// -----------------------------------------------------------------------------
// 📤 Configure Maven publication after evaluation phase.
afterEvaluate {
    publishing {
        publications {
            // Create a Maven publication named "release"
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.zs.compose"
                artifactId = "theme"
                version = "3.0.0-dev01"
            }
        }
    }
}