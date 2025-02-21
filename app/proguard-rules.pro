# Add project specific ProGuard rules here.

# Behold model klasser
-keep class com.example.timeregistrering.domain.model.** { *; }
-keep class com.example.timeregistrering.data.database.** { *; }

# Behold Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Behold Hilt
-keep class dagger.hilt.android.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Behold Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Google APIs
-keep class com.google.api.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.api.**
-dontwarn com.google.android.gms.**

# Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Generelle regler
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

# Security
-keep class androidx.security.crypto.** { *; }
-keep class javax.crypto.** { *; }

# Geofencing
-keep class com.google.android.gms.location.** { *; }
-keep class * extends com.google.android.gms.location.GeofencingClient { *; }

# Excel håndtering
-keep class org.apache.poi.** { *; }
-keep class org.openxmlformats.** { *; }

# Crash reporting
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }

# ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
