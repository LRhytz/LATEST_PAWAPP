plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

hilt {
    enableAggregatingTask = false
}


android {
    namespace   = "com.ucb.pawapp"
    compileSdk  = 34

    defaultConfig {
        applicationId             = "com.ucb.pawapp"
        minSdk                    = 24
        targetSdk                 = 34
        versionCode               = 1
        versionName               = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {

    implementation ("com.google.android.material:material:1.12.0")

    implementation("androidx.compose.compiler:compiler:1.5.8")

    // 1) Core Compose & Activity integration
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")

    // 2) Material 3 components
    implementation("androidx.compose.material3:material3:1.1.0")

    // 3) Extended icons (for filter/search/dropdowns)
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // 5) Debug‑only tooling support (Compose previews, etc)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")

      // ─── Compose Foundation (lazy lists, layouts, etc) ──────
    implementation("androidx.compose.foundation:foundation:1.5.0")

      // ─── Coil‑Compose for AsyncImage ───────────────────────
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")


    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // UI & Lifecycle
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.airbnb.android:lottie:6.0.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // 🔥 Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.3.0"))

       // All these will now use the versions from the BoM:
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore:24.8.0")
    implementation ("com.google.firebase:firebase-database-ktx")


    // Activity KTX
    implementation("androidx.activity:activity-ktx:1.7.2")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
