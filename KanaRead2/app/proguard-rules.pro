# =============================================================================
# KanaRead - ProGuard / R8 rules for release builds
# =============================================================================
# Most modern AndroidX libraries (Compose, Lifecycle, Activity, Navigation,
# Room runtime) already ship their own consumer-proguard rules, so we only
# need rules for our OWN code and for edge cases R8 doesn't catch.
# =============================================================================

# ---- Keep our Application + entry points referenced from the manifest ------
-keep class com.davidbadin.kanaread.KanaReadApplication { *; }
-keep class com.davidbadin.kanaread.MainActivity { *; }

# ---- Room: keep entities, DAOs, and the generated _Impl classes -------------
# Room generates *_Impl classes via KSP at compile time; they're referenced
# reflectively at runtime via Room.databaseBuilder(...). Keep everything in
# our data package to be safe.
-keep class com.davidbadin.kanaread.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <methods>;
}

# Kotlin metadata is needed by some reflection-based libraries.
-keep class kotlin.Metadata { *; }

# Coroutines: keep volatile fields used internally.
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Strip verbose log calls in release builds (smaller binary, cleaner logcat).
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
