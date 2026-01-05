plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // REMOVE THESE TEMPORARILY to reduce memory:
     id("kotlin-kapt")
     id("dagger.hilt.android.plugin")
     id("kotlin-parcelize")
}

android {
    namespace = "com.vola.app"
    compileSdk = 30  // DOWNGRADE from 34

    defaultConfig {
        applicationId = "com.vola.app"
        minSdk = 21  // DOWNGRADE from 26
        targetSdk = 30  // DOWNGRADE from 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // CRITICAL FOR LOW MEMORY
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // DISABLE for now
            isShrinkResources = false  // DISABLE
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),  // Use non-optimized
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            // Reduce memory in debug
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8  // DOWNGRADE from 11
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"  // DOWNGRADE from 11
    }
    
    dexOptions {
        javaMaxHeapSize = "256m"  // REDUCE from 4g
        maxProcessCount = 1
        preDexLibraries = false  // IMPORTANT!
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"  // OLDER VERSION
    }
}

dependencies {
    // === MINIMAL CORE ===
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    
    // === COMPOSE (MINIMAL) ===
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))  // OLDER
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.7.0")
    
    // === MULTIDEX (MUST HAVE) ===
    implementation("androidx.multidex:multidex:2.0.1")
    
    // === REMOVE THESE TEMPORARILY ===
    // NO Hilt
    // NO Room  
    // NO CameraX
    // NO Firebase
    // NO Charts
    // NO DataStore
    // NO Navigation
    // NO WorkManager
    // NO Biometric
    
    // === Testing (minimal) ===
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}