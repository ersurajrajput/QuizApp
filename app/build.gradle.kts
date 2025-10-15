plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.ersurajrajput.quizapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ersurajrajput.quizapp"
        minSdk = 24
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
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
    implementation(libs.firebase.database)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.cardview)
//    implementation(libs.cardview.v7)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")



    implementation ("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor ("com.github.bumptech.glide:compiler:5.0.5")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta08")

    // youtube player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    ///// cloudnary
    implementation("com.cloudinary:cloudinary-android:3.1.2")
    // Coil for Jetpack Compose (image loading)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.gridlayout:gridlayout:1.1.0")

}
afterEvaluate {
    tasks.filter { it.name.contains("mergeNativeLibs") }.forEach { task ->
        task.doLast {
            val soFiles = fileTree("$buildDir/intermediates/merged_native_libs/") {
                include("**/*.so")
            }
            soFiles.forEach { file ->
                println("Re-aligning: ${file.name}")
                try {
                    exec {
                        commandLine("patchelf", "--page-size", "65536", file.absolutePath)
                    }
                } catch (e: Exception) {
                    println("Skipping ${file.name}: patchelf not found or failed.")
                }
            }
        }
    }
}
