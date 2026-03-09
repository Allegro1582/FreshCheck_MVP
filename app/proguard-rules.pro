# Add project specific ProGuard rules here.
# By default, the rules in this file are appended at the end of the specified
# ProGuard configuration file.

# For Room
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomOpenHelper

# For ML Kit
-keep class com.google.mlkit.** { *; }

# For CameraX
-keep class androidx.camera.** { *; }
