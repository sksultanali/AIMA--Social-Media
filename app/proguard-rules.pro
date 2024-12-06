-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepattributes *Annotation*

-dontwarn com.razorpay.**
-keep class com.razorpay.** {*;}

-optimizations !method/inlining/*

-keepclasseswithmembers class * {
  public void onPayment*(...);
}

# Keep all model classes that are used with Firebase
-keepclassmembers class com.developerali.aima.Models.** {
   public <init>(...);
}

# Keep all methods and fields for Firebase model classes
-keep class com.developerali.aima.Models.** { *; }

# Prevent obfuscation of Firebase core classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**