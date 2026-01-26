#
# FastTimes ProGuard Configuration
#

# --- General Android & Debugging ---

# Retain line numbers and source file attributes for useful stack traces in crash reporters (like Firebase/Sentry).
-keepattributes SourceFile,LineNumberTable

# Retain annotations for libraries that use reflection at runtime.
-keepattributes *Annotation*

# Retain generic signatures for libraries that use reflection to determine types (e.g., Serialization).
-keepattributes Signature

# --- Kotlin Coroutines ---

# Keep the InternalCoroutinesApi used by some libraries.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    java.lang.String name;
}

# --- Hilt / Dagger ---

# Hilt handles most of this via its gradle plugin, but these ensure generated classes are preserved.
-keep class com.tmdev.fasttimes.**_HiltModules* { *; }
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }

# --- Room ---

# Room generates '_Impl' classes that need to be preserved.
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase {
    protected androidx.sqlite.db.SupportSQLiteOpenHelper createOpenHelper(androidx.room.DatabaseConfiguration);
    protected androidx.room.InvalidationTracker createInvalidationTracker();
}

# --- Kotlinx Serialization ---

# Keep classes annotated with @Serializable and their Companion serializers.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.tmdev.fasttimes.**.Companion {
    *** serializer(...);
}
-keepclasseswithmembers class com.tmdev.fasttimes.** {
    @kotlinx.serialization.Serializable <fields>;
}

# --- Jetpack Compose ---

# Ensure Compose state and stability markers aren't stripped if they are used by external tools.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# --- App Specific Models ---

# If you use reflection for any models (e.g., passing as NavArgs), keep them here.
# -keep class com.tmdev.fasttimes.data.model.** { *; }
