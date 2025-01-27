plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.apex.sunrisesunsetapp"
    compileSdk = 35  // Update to 35

    defaultConfig {
        applicationId = "com.apex.sunrisesunsetapp"
        minSdk = 29
        targetSdk = 35  // Ensure targetSdk is also updated to 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation (libs.appcompat.v140)   // Ensure this is the correct version
    implementation (libs.material.v160)  // Keep the latest version
    implementation (libs.activity)  // Ensure compatibility with Activity library
    implementation (libs.constraintlayout.v213) // Correct version
    implementation (libs.okhttp.v490) // OkHttp for API calls
    implementation (libs.play.services.location.v1800) // Location Services
    testImplementation (libs.junit)
    androidTestImplementation (libs.junit.v113)
    androidTestImplementation (libs.androidx.espresso.core)
}
