plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "site.arian.threeemail"
 compileSdk = 35
 defaultConfig { applicationId = "site.arian.threeemail"; minSdk = 26; targetSdk = 34; versionCode = 1; versionName = "1.0.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 packaging { resources { excludes += setOf("META-INF/NOTICE.md", "META-INF/LICENSE.md", "META-INF/DEPENDENCIES", "META-INF/LICENSE.txt", "META-INF/NOTICE.txt") } }
}
dependencies {
 implementation("androidx.core:core-ktx:1.15.0")
 implementation("androidx.activity:activity-ktx:1.10.0")
 implementation("androidx.appcompat:appcompat:1.7.0")
 implementation("com.google.android.material:material:1.12.0")
 implementation("com.sun.mail:android-mail:1.6.7")
}
