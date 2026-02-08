import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "id.onyet.app.notifylog"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.onyet.app.notifylog"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Signing configuration for release AAB. Values are read from Gradle project properties,
    // a local `keystore.properties` file, or environment variables to avoid committing secrets to the repository.
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProps.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            val ksFile = (project.findProperty("keystoreFile") as String?)
                ?: System.getenv("KEYSTORE_FILE")
                ?: keystoreProps.getProperty("storeFile")

            if (ksFile != null) {
                // Try file path relative to this module
                val candidate = file(ksFile)
                if (candidate.exists()) {
                    storeFile = candidate
                } else {
                    // Fall back to project root relative path
                    val rootCandidate = rootProject.file(ksFile)
                    if (rootCandidate.exists()) {
                        storeFile = rootCandidate
                    } else {
                        // Use provided path as-is (will fail validation later with clear message)
                        storeFile = candidate
                    }
                }

                storePassword = (project.findProperty("keystorePassword") as String?)
                    ?: System.getenv("KEYSTORE_PASSWORD")
                    ?: keystoreProps.getProperty("storePassword")
                keyAlias = (project.findProperty("keyAlias") as String?)
                    ?: System.getenv("KEY_ALIAS")
                    ?: keystoreProps.getProperty("keyAlias")
                keyPassword = (project.findProperty("keyPassword") as String?)
                    ?: System.getenv("KEY_PASSWORD")
                    ?: keystoreProps.getProperty("keyPassword")
            }
        }
    }

    // Ensure all language resources are included in release bundles so in-app language switching
    // works even when Google Play language splits are enabled. Otherwise languages not matched
    // to the device locale may not be delivered with the app and the app will fall back to
    // the default locale (usually English).
    bundle {
        language {
            enableSplit = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    
    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager (periodic background work)
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Play In-App Update (migrated from Play Core)
    implementation("com.google.android.play:app-update:2.1.0")
    // Optional: Kotlin extensions for Play In-App Update
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.11.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
