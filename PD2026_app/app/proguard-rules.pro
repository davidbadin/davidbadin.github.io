# Add project-specific ProGuard rules here.

# Hilt — generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

# Navigation Compose type-safe routes
-keep class sk.punkacidetom.pd2026.** { *; }

# Gson / JSON cache
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keepclassmembers class sk.punkacidetom.pd2026.core.data.cache.** { *; }

# Coil
-dontwarn okhttp3.**

# Firebase Messaging
-keep class com.google.firebase.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Spotify SDK
-keep class com.spotify.** { *; }
-dontwarn com.spotify.**
