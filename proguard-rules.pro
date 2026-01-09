# Kotlinx Serialization
-dontwarn kotlinx.serialization.**
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep all serializable classes with their @Serializable annotation
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}

# Keep serializers
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}

# Keep serializable classes and their properties
-if @kotlinx.serialization.Serializable class **
-keep class <1> {
    static <1>$Companion Companion;
}

# Keep specific serializer classes
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serialization descriptors
-keep class kotlinx.serialization.descriptors.** { *; }

# Specifically keep AppSettings and its serializer
-keep class data.AppSettings { *; }
-keep class data.AppSettings$$serializer { *; }

# Keep all data classes and their serializers
-keep class data.** { *; }
-keep class data.**$$serializer { *; }

# SLF4J
-dontwarn org.slf4j.**
-dontnote org.slf4j.**

# Keep META-INF services
-keepdirectories META-INF/services/
-keep class META-INF.services.** { *; }

# JNA
-dontwarn java.awt.*
-dontwarn sun.font.**
-dontwarn sun.swing.**
-dontwarn net.miginfocom.swing.**
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.Structure {
    public *;
}

# Keep all JNA internal classes and their synthetic accessors
-keep,allowobfuscation class com.sun.jna.internal.** { *; }
-keepclassmembers class com.sun.jna.internal.** {
    *;
}

# JNA Cleaner synthetic accessors
-dontwarn com.sun.jna.internal.Cleaner$CleanerThread

# OkHttp optional dependencies
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.graalvm.**
-dontwarn com.oracle.svm.**

# OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keepclassmembers class okhttp3.** { *; }
-keepclassmembers class okio.** { *; }

# OkHttp3 platform implementations
-keep class okhttp3.internal.platform.** { *; }
-keep interface okhttp3.** { *; }

# Okio
-keep class okio.** { *; }
-keepnames class okio.** { *; }

# VLCj
-keep class uk.co.caprica.vlcj.** { *; }
-dontwarn com.sun.awt.AWTUtilities

# Compose and PreCompose Navigation
-keep class moe.tlaster.precompose.** { *; }
-keep class androidx.compose.foundation.gestures.** { *; }
-keep class androidx.compose.animation.core.** { *; }
-dontwarn androidx.compose.foundation.gestures.AnchoredDraggableKt
-dontwarn androidx.compose.foundation.gestures.AnchoredDraggableState
-dontwarn androidx.compose.animation.core.SeekableTransitionState

# Kotlin internal classes
-dontwarn kotlin.jvm.internal.EnhancedNullability
-dontwarn kotlin.concurrent.atomics.**

# Keep Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Suppress all notes about duplicate classes
-dontnote **