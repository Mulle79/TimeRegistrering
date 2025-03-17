object Versions {
    const val kotlin = "1.9.22"
    const val androidGradlePlugin = "8.3.0"
    const val ksp = "1.9.22-1.0.16"
    const val hilt = "2.48"
    const val compose = "1.6.1"
    const val composeMaterial3 = "1.2.0"
    const val lifecycleRuntime = "2.7.0"
    const val activityCompose = "1.8.2"
    const val room = "2.6.1"
    const val paging = "3.2.1"
    const val dokka = "1.9.10"
}

object Libs {
    object AndroidX {
        const val core = "androidx.core:core-ktx:1.12.0"
        const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntime}"
        const val activity = "androidx.activity:activity-compose:${Versions.activityCompose}"
        
        object Compose {
            const val ui = "androidx.compose.ui:ui:${Versions.compose}"
            const val graphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
            const val preview = "androidx.compose.ui:ui-tooling-preview:${Versions.compose}"
            const val material3 = "androidx.compose.material3:material3:${Versions.composeMaterial3}"
            const val tooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
            const val manifest = "androidx.compose.ui:ui-test-manifest:${Versions.compose}"
        }

        object Room {
            const val runtime = "androidx.room:room-runtime:${Versions.room}"
            const val compiler = "androidx.room:room-compiler:${Versions.room}"
            const val ktx = "androidx.room:room-ktx:${Versions.room}"
        }

        object Paging {
            const val runtime = "androidx.paging:paging-runtime-ktx:${Versions.paging}"
            const val compose = "androidx.paging:paging-compose:${Versions.paging}"
        }
    }

    object Hilt {
        const val android = "com.google.dagger:hilt-android:${Versions.hilt}"
        const val compiler = "com.google.dagger:hilt-compiler:${Versions.hilt}"
    }

    object Test {
        const val junit = "junit:junit:4.13.2"
        const val androidJunit = "androidx.test.ext:junit:1.1.5"
        const val espresso = "androidx.test.espresso:espresso-core:3.5.1"
    }
}
