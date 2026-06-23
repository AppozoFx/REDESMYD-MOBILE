# =============================================================
# REDES Mobile — ProGuard / R8 rules (release)
# =============================================================

# --- Stack traces legibles en Crashlytics / Firebase ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# --- Coroutines ---
-dontwarn kotlinx.coroutines.debug.*
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# --- OkHttp ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Data classes (sesión, alertas, tracking) ---
# Necesario si Firestore deserializa vía .toObject() o si OkHttp
# parsea JSON usando reflexión sobre los campos del modelo.
-keep class com.redes.app.data.** { *; }
-keepclassmembers class com.redes.app.data.** {
    public <init>();
    <fields>;
}

# --- Firebase ---
# Firebase ships its own rules via AAR; estas son de respaldo.
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# --- Jetpack Compose ---
# Compose BOM ships its own rules; no se necesita nada extra.

# --- Google Maps Compose ---
-dontwarn com.google.maps.**
