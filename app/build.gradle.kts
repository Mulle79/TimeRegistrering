import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.io.FileInputStream

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    // Dokka-plugin fjernet for at løse byggeproblemer
    id("jacoco")
}

// Signing configuration
val keystorePropertiesFile = rootProject.file("keystore.properties")
val useSigningConfig = keystorePropertiesFile.exists()
val keystoreProperties = Properties()
if (useSigningConfig) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = Versioning.App.applicationId
    compileSdk = Versioning.App.compileSdk

    defaultConfig {
        applicationId = Versioning.App.applicationId
        minSdk = Versioning.App.minSdk
        targetSdk = Versioning.App.targetSdk
        versionCode = Versioning.versionCode
        versionName = Versioning.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        multiDexEnabled = true

        manifestPlaceholders += mapOf(
            "MAPS_API_KEY" to (findProperty("MAPS_API_KEY")?.toString() ?: ""),
            "BASE_URL" to (findProperty("BASE_URL")?.toString() ?: "https://api.example.com"),
            "GOOGLE_CLIENT_ID" to (findProperty("GOOGLE_CLIENT_ID")?.toString() ?: ""),
            "GOOGLE_CLIENT_SECRET" to (findProperty("GOOGLE_CLIENT_SECRET")?.toString() ?: "")
        )
    }

    // Signing configuration
    if (useSigningConfig) {
        signingConfigs {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (useSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isEnabled = true
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports"
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/R.class",
                    "**/R$*.class",
                    "**/BuildConfig.class",
                    "**/Manifest*.*",
                    "android/**/*.*",
                    "**/*_Impl*.*",
                    "**/*Module*.*",
                    "**/*Dagger*.*",
                    "**/*Hilt*.*"
                )
            }
        })
    )
}

tasks.withType<JacocoCoverageVerification> {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal("0.7") // 70% coverage requirement
            }
        }
        rule {
            enabled = false
            element = "CLASS"
            includes = listOf("com.example.timeregistrering.*")
            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = BigDecimal("0.3")
            }
        }
    }
}

dependencies {
    // AndroidX
    implementation(Libs.AndroidX.core)
    implementation(Libs.AndroidX.lifecycle)
    implementation(Libs.AndroidX.activity)
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")

    // Compose
    implementation(Libs.AndroidX.Compose.ui)
    implementation(Libs.AndroidX.Compose.graphics)
    implementation(Libs.AndroidX.Compose.preview)
    implementation(Libs.AndroidX.Compose.material3)
    implementation("androidx.compose.material:material:1.6.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")
    debugImplementation(Libs.AndroidX.Compose.tooling)
    debugImplementation(Libs.AndroidX.Compose.manifest)

    // Room
    implementation(Libs.AndroidX.Room.runtime)
    implementation(Libs.AndroidX.Room.ktx)
    ksp(Libs.AndroidX.Room.compiler)

    // Hilt
    implementation(Libs.Hilt.android)
    ksp(Libs.Hilt.compiler)
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Paging
    implementation(Libs.AndroidX.Paging.runtime)
    implementation(Libs.AndroidX.Paging.compose)

    // Google
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Work
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // POI
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation(Libs.Test.junit)
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspTest("com.google.dagger:hilt-android-compiler:2.48")
    androidTestImplementation(Libs.Test.androidJunit)
    androidTestImplementation(Libs.Test.espresso)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.1")
}
