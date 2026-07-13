plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mihai.dailyhabit"
    compileSdk = 37
    // API 37.1 is installed, but AGP 9.4.0-alpha03 maps extension=1 to the
    // unavailable SDK target android-37.0-ext1. See RESEARCH.md for the build proof.

    defaultConfig {
        applicationId = "com.mihai.dailyhabit"
        minSdk = 35
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true; buildConfig = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.window)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.ai.edge.litertlm)
    implementation(libs.pdfbox.android)
    implementation(libs.work.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
