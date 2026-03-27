import java.util.Properties


plugins {
    alias(libs.plugins.android.application)

}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("apikeys.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseAnonKey = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

val googleWebClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""

android {
    namespace = "com.android.labassist"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.android.labassist"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"$supabaseUrl\""
        )

        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"$supabaseAnonKey\""
        )

        buildConfigField(
            type = "String",
            name = "GOOGLE_WEB_CLIENT_ID",
            value = "\"$googleWebClientId\""
        )
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
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    Splash Screen Dependency
    implementation(libs.core.splashscreen)

    implementation(libs.preference)

//    Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

//    Retrofit and GSON converter
    implementation(libs.retrofit)
// Gson converter (for JSON parsing)
    implementation(libs.converter.gson)

    implementation(libs.security.crypto)

//  Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

//    Google Sign-in
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.lifecycle.livedata)

    implementation(libs.play.services.auth)

    // The core Credential Manager API
    implementation("androidx.credentials:credentials:1.2.2")
// Play Services integration for Credential Manager
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
// Google ID library for parsing the modern tokens
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
}