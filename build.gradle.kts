plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

allprojects {
    version = project.findProperty("VERSION_NAME") as String? ?: "2.0.37.1"
}
