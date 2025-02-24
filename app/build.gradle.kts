plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.sharecipe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sharecipe"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("gson-2.2.2.jar"))
    implementation(libs.navigation.runtime)
    implementation(libs.play.services.tasks)
    implementation(libs.firebase.auth)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.firebase.database)
    implementation(libs.navigation.fragment)
    implementation(files("gson-2.2.2.jar"))
    implementation("com.google.firebase:firebase-storage:21.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}