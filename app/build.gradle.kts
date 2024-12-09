plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.jonathan.mymaps"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jonathan.mymaps"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location) // Location services
    implementation(libs.play.services.maps) // Google Maps SDK
    implementation(libs.maps.compose) // Jetpack Compose Maps library
    implementation(libs.work.runtime) // WorkManager
    implementation(libs.workmanager.koin) // Koin for WorkManager
    implementation(libs.room.runtime) // Room runtime
    implementation(libs.room.ktx) // Room KTX
    //ksp(libs.room.compiler) // Room compiler

    //implementation("androidx.work:work-runtime-ktx:3.5.3")
    //implementation("io.insert-koin:koin-androidx-workmanager:3.5.3")
    //implementation("com.android.tools.build:gradle:3.6.0-rc01")
    //implementation(kotlin("gradle-plugin", version = "1.3.61"))
    // https://mvnrepository.com/artifact/androidx.work/work-runtime-ktx
    //runtimeOnly("androidx.work:work-runtime-ktx:2.10.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    //androidTestImplementation(libs.work.testing)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}