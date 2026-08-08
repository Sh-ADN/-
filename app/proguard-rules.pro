# ProGuard rules for release build size optimization and shrinking

-optimizationpasses 5
-dontpreverify
-repackageclasses ''
-allowaccessmodification

# Preserve Moshi annotations and generated adapters
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Room persistence library
-keep class * extends androidx.room.RoomDatabase

# Strip debug log statements in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

