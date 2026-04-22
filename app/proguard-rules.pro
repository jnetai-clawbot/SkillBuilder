# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.jnetai.skillbuilder.data.** { *; }
-keep class androidx.room.** { *; }
-dontwarn kotlinx.coroutines.**