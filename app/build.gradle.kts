import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// Secrets locales — nunca committear este archivo
val localProps = Properties().also { props ->
    rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use { props.load(it) }
}

val debugApiBaseUrl = providers.gradleProperty("redes.apiBaseUrl.debug")
    .orElse(providers.gradleProperty("redes.apiBaseUrl"))
    .orElse("")
    .get()
val releaseApiBaseUrl = localProps.getProperty("redes.apiBaseUrl.release")
    ?: providers.gradleProperty("redes.apiBaseUrl.release").orElse("").get()
val mapsApiKey = localProps.getProperty("redes.mapsApiKey")
    ?: providers.gradleProperty("redes.mapsApiKey").orElse("").get()

android {
    namespace = "com.redes.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.redesmyd.mobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 12
        versionName = "1.0.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    signingConfigs {
        create("release") {
            val keyFile = localProps.getProperty("redes.keystore.file")
            val storePwd = localProps.getProperty("redes.keystore.storePassword")
            val keyAl   = localProps.getProperty("redes.keystore.keyAlias")
            val keyPwd  = localProps.getProperty("redes.keystore.keyPassword")
            if (keyFile != null && storePwd != null && keyAl != null && keyPwd != null) {
                storeFile    = file(keyFile)
                storePassword = storePwd
                keyAlias      = keyAl
                keyPassword   = keyPwd
            }
        }
    }

    buildTypes {
        // ── debug ── servidor local  →  necesita: pnpm --filter web dev --hostname 0.0.0.0
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"debug\"")
        }
        // ── staging ── backend de producción, sin minificación, sin firma especial
        // Usá este variant cuando no querés levantar el servidor local.
        create("staging") {
            initWith(getByName("debug"))
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"staging\"")
            matchingFallbacks += listOf("debug")
        }
        // ── release ── producción, firmado, minificado
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"release\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:maps-compose:4.4.1")
}
