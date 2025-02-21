object Versioning {
    private const val MAJOR = 1
    private const val MINOR = 0
    private const val PATCH = 0

    private const val BUILD_NUMBER = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0

    val versionCode = MAJOR * 10000 + MINOR * 1000 + PATCH * 100 + BUILD_NUMBER
    val versionName = "$MAJOR.$MINOR.$PATCH"

    object App {
        const val applicationId = "com.example.timeregistrering"
        const val minSdk = 26
        const val targetSdk = 34
        const val compileSdk = 34
    }
}
