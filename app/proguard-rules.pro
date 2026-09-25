-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.gorman.ourmemoryapp.domain.models.Veteran { *; }
-keep class com.gorman.ourmemoryapp.domain.models.Burial { *; }
-keep class com.gorman.ourmemoryapp.domain.models.Tour { *; }
-keep class com.gorman.ourmemoryapp.domain.models.TourStop { *; }
-keep class com.gorman.ourmemoryapp.domain.models.*Translation { *; }
-keep class com.gorman.ourmemoryapp.data.**.model.** { *; }
-keep class com.gorman.ourmemoryapp.data.models.** { *; }

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
