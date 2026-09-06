plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.willykez.liturgx"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.willykez.liturgx"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
    }

    // Release signing is driven entirely by environment variables so the real keystore and
    // its passwords never touch source control. Locally these are simply unset and a release
    // build stays unsigned (Android Studio will warn, which is correct); in CI, the
    // ".github/workflows/release.yml" workflow decodes the KEYSTORE_B64 repository secret to
    // a file and exports the other three secrets (KEY_ALIAS, KEY_PASSWORD, STORE_PASSWORD)
    // as env vars before invoking Gradle, so this block picks them up automatically.
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("STORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only wire the signing config up when a keystore is actually available (CI, or a
            // local build with the four env vars exported by hand) -- otherwise leave the
            // release build type unsigned rather than pointing it at a signing config with
            // null fields, which fails the build the moment you try to configure it.
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // The bundled Swahili lectionary DB, and the bundled Swahili Bible used to resolve
    // citations into full verse text, both ship read-only in assets/ and are copied to
    // app-internal storage on first use (see data/DatabaseProvider.kt and
    // data/bible/BibleDatabaseHelper.kt).
    androidResources {
        noCompress += "db"
        noCompress += "sqlite"
    }
}

// AGP 9's auto-applied Kotlin plugin no longer exposes android { kotlinOptions {} } —
// jvmToolchain is the modern, plugin-agnostic way to pin the Kotlin/Java target.
kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Resolves a citation's Scripture text off the main thread (data/bible/BibleRepository.kt).
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
