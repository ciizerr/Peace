import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.nami.peace"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nami.peace"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        if (keystoreFile.exists()) {
            properties.load(keystoreFile.inputStream())
        }
        // 2. Extract the key (or use a dummy one if missing to prevent build errors)
        val apiKey = properties.getProperty("GEMINI_API_KEY") ?: ""

        // 3. Bake it into the app as a static variable
        buildConfigField("String", "GEMINI_API_KEY", apiKey)
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // 1. The Brain: Google AI Client SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")


    // 2. The Memory: Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Coroutine support
    ksp("androidx.room:room-compiler:$room_version") // Requires ksp plugin in top-level gradle

    // 3. Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // 4. Fonts (For Poppins)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.0")

    // 5. Extended Icons (for the Sparkle)
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}