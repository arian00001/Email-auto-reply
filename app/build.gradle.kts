
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "site.arian00001.devstyleautoreply"
    compileSdk = 35

    defaultConfig {
        applicationId = "site.arian00001.devstyleautoreply"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {
    // Android UI and core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // WorkManager / CoroutineWorker / Result.retry()
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // EncryptedSharedPreferences / MasterKey
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Gmail IMAP / SMTP
    implementation("com.sun.mail:android-mail:1.6.7")
}
