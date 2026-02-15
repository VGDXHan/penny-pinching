# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data classes for Gson
-keepattributes Signature
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# Keep data models
-keep class com.example.voicebill.domain.model.** { *; }
-keep class com.example.voicebill.data.remote.dto.** { *; }

# OpenAI SDK
-dontwarn com.openai.**
-keep class com.openai.** { *; }
