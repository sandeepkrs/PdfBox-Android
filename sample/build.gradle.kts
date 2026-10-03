plugins {
    alias(libs.plugins.android.application)
}

val versionNameProp = project.findProperty("VERSION_NAME") as String? ?: "2.0.37.0-SNAPSHOT"
val versionCodeProp = (project.findProperty("VERSION_CODE") as String?)?.toInt() ?: 1

android {
    namespace = "com.tom_roush.pdfbox.sample"
    compileSdk = (project.findProperty("ANDROID_BUILD_SDK_VERSION") as String?)?.toInt() ?: 37

    defaultConfig {
        applicationId = "com.tom_roush.pdfbox.sample"
        minSdk = (project.findProperty("ANDROID_BUILD_MIN_SDK_VERSION") as String?)?.toInt() ?: 23
        targetSdk = (project.findProperty("ANDROID_BUILD_TARGET_SDK_VERSION") as String?)?.toInt() ?: 37
        versionName = versionNameProp
        versionCode = versionCodeProp
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    lint {
        abortOnError = false
    }
}

// BouncyCastle 1.85 packaging bug: bcprov and bcutil both ship the same
// org.bouncycastle.asn1.iana.* classes, causing checkDuplicateClasses to fail.
configurations.all {
    exclude(group = "org.bouncycastle", module = "bcutil-jdk15to18")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
}

dependencies {
    implementation(project(":library"))
    implementation(libs.androidx.appcompat)

    // Read JPX images
    implementation(libs.jp2.android)
}
