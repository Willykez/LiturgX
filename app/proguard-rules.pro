# Kotlin/Compose reflection metadata used by tooling, not by the app at runtime -- safe to strip.
-dontwarn kotlin.**

# Room isn't used by this app (raw SQLite via DatabaseProvider/DAO classes), so no @Keep needed
# for entities. Keep only what's actually reflection-accessed:

# Kept for the Bible/lectionary/reading share JSON-free data classes that cross process
# boundaries via Intent extras (EXTRA_TEXT etc. are plain Strings, no serialization needed) --
# nothing here actually needs reflection, listed for clarity that this was checked, not skipped.

# TextToSpeech's UtteranceProgressListener is called by the system via its public API surface,
# already safe under default keep rules for anything extending an Android SDK class.

# Coroutines' internal use of reflection for volatile fields.
-dontwarn kotlinx.coroutines.**
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# AndroidX startup / reflection-based Initializer lookups (used transitively by some libraries).
-keep class androidx.startup.** { *; }

# Compose compiler-generated classes carry no runtime reflection needs; the Compose Gradle
# plugin already ships consumer rules for its own runtime, so nothing app-specific is required
# beyond what's listed here.
