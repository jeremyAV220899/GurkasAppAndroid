plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cloud.gurkasapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.cloud.gurkasapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // ============================
    // CAMERAX
    // ============================

    val cameraxVersion = "1.4.2"

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ============================
    // ESCÁNER QR - ML KIT
    // ============================

    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ============================
    // API
    // ============================
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ============================
    // FOTO
    // ============================
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // ML Kit - detección facial
    implementation("com.google.mlkit:face-detection:16.1.7")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.17.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

}