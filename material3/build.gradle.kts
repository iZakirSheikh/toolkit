plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id(libs.plugins.maven.publish.get().getPluginId())
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.primex.material3"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("proguard-rules.pro")
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
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(project(path = ":core-ktx"))
    compileOnly(libs.compose.foundation.layout)
    compileOnly(libs.compose.ui)
    compileOnly(libs.compose.material3)
    implementation(libs.compose.ui.util)
    // implementation "androidx.compose.ui:ui-tooling:$compose_version"
    // implementation "androidx.core:core-ktx:1.9.0"
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
                groupId = "com.primex.toolkit"
                artifactId = "material3"
                version = "1.0.0"
            }
        }
    }
}

