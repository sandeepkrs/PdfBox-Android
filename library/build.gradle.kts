import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
}

val minSdkProp: String by project
val compileSdkProp: String by project
val versionNameProp = project.findProperty("VERSION_NAME") as String? ?: "2.0.37.0-SNAPSHOT"

android {
    namespace = "com.tom_roush.pdfbox"
    compileSdk = (project.findProperty("ANDROID_BUILD_SDK_VERSION") as String?)?.toInt() ?: 37

    defaultConfig {
        minSdk = (project.findProperty("ANDROID_BUILD_MIN_SDK_VERSION") as String?)?.toInt() ?: 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["notAnnotation"] = "androidx.test.filters.FlakyTest"
        consumerProguardFiles("consumer-proguard-rules.txt")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    sourceSets {
        getByName("test") {
            resources.srcDirs("src/main/assets", "src/test/resources")
        }
        getByName("androidTest") {
            assets.srcDirs("src/test/resources")
        }
    }

    lint {
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        encoding = "UTF-8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

tasks.withType<Test> {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    testLogging {
        events(
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.FAILED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR
        )
        showExceptions = true
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true

        afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
            if (desc.parent == null) {
                val output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
                val startItem = "|  "
                val endItem = "  |"
                val repeatLength = startItem.length + output.length + endItem.length
                println("\n" + ("-".repeat(repeatLength)) + "\n" + startItem + output + endItem + "\n" + ("-".repeat(repeatLength)))
            }
        }))
    }
}

dependencies {
    api(libs.bouncycastle.bcprov)
    api(libs.bouncycastle.bcpkix)
    api(libs.bouncycastle.bcutil)

    // for jpeg2000 decode/encode
    compileOnly(libs.jp2.android)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.java.diffutils)

    androidTestImplementation(libs.androidx.test.runner)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.tom-roush"
                artifactId = "pdfbox-android"
                version = versionNameProp

                pom {
                    name.set("PdfBox-Android")
                    description.set("The Apache PdfBox project ported to work on Android")
                    url.set("https://github.com/TomRoush/PdfBox-Android")
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            name.set("Tom Roush")
                            email.set("tom@tom-roush.com")
                            organization.set("PdfBox-Android Developers")
                            organizationUrl.set("https://github.com/TomRoush/PdfBox-Android")
                        }
                    }
                    scm {
                        connection.set("scm:git:git@github.com:TomRoush/PdfBox-Android.git")
                        developerConnection.set("scm:git:ssh@github.com:TomRoush/PdfBox-Android.git")
                        url.set("https://github.com/TomRoush/PdfBox-Android")
                    }
                }
            }
        }
        repositories {
            maven {
                val isReleaseVersion = !versionNameProp.endsWith("SNAPSHOT")
                val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(if (isReleaseVersion) releaseRepo else snapshotRepo)
                credentials {
                    username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                    password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }

    signing {
        val signingKeyId = project.findProperty("signingKeyId") as String?
        val signingKey = project.findProperty("signingKey") as String?
        val signingPassword = project.findProperty("signingPassword") as String?
        if (!signingKey.isNullOrEmpty()) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications["release"])
        }
    }
}
