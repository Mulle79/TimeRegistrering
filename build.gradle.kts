// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version Versions.androidGradlePlugin apply false
    id("org.jetbrains.kotlin.android") version Versions.kotlin apply false
    id("com.google.devtools.ksp") version Versions.ksp apply false
    id("com.google.dagger.hilt.android") version Versions.hilt apply false
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.10"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add any buildscript dependencies here
    }
}

subprojects {
    apply(plugin = "jacoco")

    tasks.withType<Test> {
        configure<JacocoTaskExtension> {
            isEnabled = true
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    tasks.withType<JacocoReport> {
        dependsOn("test")
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }

    // Minimum coverage requirements
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
                includes = listOf("org.gradle.*")
                limit {
                    counter = "LINE"
                    value = "TOTALCOUNT"
                    maximum = BigDecimal("0.3")
                }
            }
        }
    }
}

// Documentation configuration
subprojects {
    // Configure documentation tasks
    tasks.register("generateDocumentation") {
        group = "documentation"
        description = "Generates the project's API documentation"
    }
}
