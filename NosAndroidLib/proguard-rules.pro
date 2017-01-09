# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\programs\android_sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep public class com.netease.cloud.nos.android.core.**{*;}
-keep public class com.netease.cloud.nos.android.utils.**{*;}
-keep public class com.netease.cloud.nos.android.exception.**{*;}
-keep public class com.netease.cloud.nos.android.constants.**{*;}
-keep public class com.netease.cloud.nos.android.http.**{*;}
-keep public class com.netease.cloud.nos.android.monitor.**{*;}
-keep public class com.netease.cloud.nos.android.pipeline.**{*;}
-keep public class com.netease.cloud.nos.android.receiver.**{*;}
-keep public class com.netease.cloud.nos.android.service.**{*;}
-keep public class com.netease.cloud.nos.android.ssl.**{*;}

# -keep public class InvalidChunkSizeException
# -keep public class InvalidParameterException

-keep class android.os.**{*;}
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
# -keep public class * extends android.support.v4.widget

-dontwarn com.sun.nio.sctp.**
-dontwarn org.jboss.marshalling.**
-dontwarn gnu.io.**
-dontwarn java.nio.channels.**
-dontwarn java.net.**
-dontwarn com.barchart.udt.**
-dontwarn com.jcraft.jzlib.**
-dontwarn com.google.protobuf.**
-dontwarn org.eclipse.jetty.npn.**
-dontwarn javax.net.ssl.**
-dontwarn org.apache.tomcat.jni.**
-dontwarn org.bouncycastle.asn1.**
-dontwarn sun.security.x509.**
-dontwarn sun.misc.**
-dontwarn javassist.**
-dontwarn org.bouncycastle.cert.jcajce.**
-dontwarn org.bouncycastle.operator.jcajce.**
-dontwarn org.bouncycastle.cert.**
-dontwarn org.bouncycastle.cert.jcajce.**
-dontwarn org.bouncycastle.jce.provider.**
-dontwarn org.bouncycastle.operator.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.log4j.**
-dontwarn org.slf4j.**

-keepclasseswithmembernames class * {     # 保持 native 方法不被混淆
    native <methods>;
}
-keepclasseswithmembers class * {         # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {         # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity { #保持类成员
   public void *(android.view.View);
}

-keepclassmembers enum * {                  # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {    # 保持Parcelable不被混淆
  public static final android.os.Parcelable$Creator *;
}
