package com.example.kitahack2025;

public class dummy {
//    -android manifest-
//    <?xml version="1.0" encoding="utf-8"?>
//<manifest xmlns:android="http://schemas.android.com/apk/res/android"
//    xmlns:tools="http://schemas.android.com/tools">
//
//    <uses-permission android:name="android.permission.INTERNET" />
//    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
//    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
//    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
//    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
//    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
//    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
//    <uses-feature android:name="android.hardware.location.gps" android:required="true" />
//    <uses-feature android:name="android.hardware.location.network" android:required="true" />
//
//    <application
//    android:allowBackup="true"
//    android:dataExtractionRules="@xml/data_extraction_rules"
//    android:fullBackupContent="@xml/backup_rules"
//    android:icon="@mipmap/ic_launcher"
//    android:label="@string/app_name"
//    android:roundIcon="@mipmap/ic_launcher_round"
//    android:supportsRtl="true"
//    android:theme="@style/Theme.KitaHack2025"
//    tools:targetApi="31">
//        <activity
//    android:name=".ChatActivity"
//    android:exported="false" />
//        <activity
//    android:name=".HomePageActivity"
//    android:exported="false" />
//        <activity
//    android:name=".LoginActivity"
//    android:exported="false" />
//        <activity
//    android:name=".SignupActivity"
//    android:exported="false" />
//        <activity
//    android:name=".MainActivity"
//    android:exported="true">
//            <intent-filter>
//                <action android:name="android.intent.action.MAIN" />
//
//                <category android:name="android.intent.category.LAUNCHER" />
//            </intent-filter>
//        </activity>
//    </application>
//
//</manifest>

//    -libs.version.toml-
//    [versions]
//    agp = "8.8.0"
//    junit = "4.13.2"
//    junitVersion = "1.2.1"
//    espressoCore = "3.6.1"
//    appcompat = "1.7.0"
//    material = "1.12.0"
//    activity = "1.10.1"
//    constraintlayout = "2.2.1"
//    firebaseFirestore = "25.1.3"
//
//            [libraries]
//    junit = { group = "junit", name = "junit", version.ref = "junit" }
//    ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
//    espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
//    appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
//    material = { group = "com.google.android.material", name = "material", version.ref = "material" }
//    activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
//    constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
//    firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore", version.ref = "firebaseFirestore" }
//
//[plugins]
//    android-application = { id = "com.android.application", version.ref = "agp" }

//    -build.gradle(:app)
//plugins {
//    id 'com.android.application'
//    id 'com.google.gms.google-services'
//}
//
//    android {
//        namespace "com.example.kitahack2025"
//        compileSdk 34
//
//        defaultConfig {
//            applicationId "com.example.kitahack2025"
//            minSdk 24
//            targetSdk 34
//            versionCode 1
//            versionName "1.0"
//
//            testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
//        }
//
//        buildTypes {
//            release {
//                minifyEnabled false
//                proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
//            }
//        }
//
//        compileOptions {
//            sourceCompatibility JavaVersion.VERSION_11
//            targetCompatibility JavaVersion.VERSION_11
//        }
//    }
//
//    dependencies {
//        // Firebase BoM
//        implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
//        implementation("com.google.firebase:firebase-analytics")
//        implementation("com.google.firebase:firebase-auth")
//        implementation("com.google.firebase:firebase-firestore")
//        implementation("com.google.firebase:firebase-storage")
//
//        // AndroidX
//        implementation("androidx.appcompat:appcompat:1.6.1")
//        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//        implementation("androidx.cardview:cardview:1.0.0")
//        implementation("androidx.recyclerview:recyclerview:1.3.2")
//        implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
//
//        // Material Design
//        implementation("com.google.android.material:material:1.11.0")
//
//        // Kotlin Coroutines
//        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
//        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
//
//        // Gemini API
//        implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
//
//        // Image Loading
//        implementation("com.github.bumptech.glide:glide:4.16.0")
//        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
//
//        // Testing
//        testImplementation("junit:junit:4.13.2")
//        androidTestImplementation("androidx.test.ext:junit:1.1.5")
//        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//
//        // Google Play Services - Auth and Identity
//        implementation("com.google.android.gms:play-services-auth:21.3.0") // Match version with Credential Manager
//        implementation("com.google.android.gms:play-services-identity:18.1.0")
//
//        // Credential Manager
//        implementation("androidx.credentials:credentials:1.3.0")
//        implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
//
//        // Other Core Dependencies
//        implementation(libs.appcompat)
//        implementation(libs.material)
//        implementation(libs.constraintlayout)
//        implementation("androidx.activity:activity-ktx:1.9.3") // Kotlin extensions for Activity
//
//        // Google Identity Services
//        implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
//        implementation("com.google.android.libraries.places:places:2.7.0")
//        implementation(libs.firebase.firestore)
//
//        // Other Libraries
//        implementation("com.makeramen:roundedimageview:2.3.0")
//        implementation("androidx.cardview:cardview:1.0.0")
//        implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
//
//        // Google Play Services Tasks (ensure latest)
//        implementation("com.google.android.gms:play-services-tasks:18.2.0")
//
//
//        // Firebase Auth and Google Play Services
//        implementation("com.google.firebase:firebase-auth")
//        implementation("com.google.android.gms:play-services-auth:21.3.0")
//
//        // SwipeRefreshLayout
//        implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
//
//        // Add Firebase Storage
//        implementation("com.google.firebase:firebase-storage-ktx")
//        implementation("com.google.firebase:firebase-storage:21.0.1")
//
//        // Add Glide for image loading (optional but recommended)
//        implementation("com.github.bumptech.glide:glide:4.16.0")
//
//        // LocalBroadcastManager
//        implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
//
//        implementation("com.google.firebase:firebase-messaging:24.1.0")
//
//        // Google Play Service Location
//        implementation("com.google.android.gms:play-services-location:21.3.0")
//        implementation("com.google.android.gms:play-services-maps:19.0.0")
//        implementation("com.google.android.libraries.places:places:3.3.0")
//
//        implementation ("com.squareup.okhttp3:okhttp:4.9.3")
//        implementation ("org.json:json:20210307")
//        // Other dependencies
//
//        // Markdown Support
//        implementation("io.noties.markwon:core:4.6.2")
//        implementation("io.noties.markwon:html:4.6.2")
//        implementation("io.noties.markwon:image:4.6.2")
//        implementation("io.noties.markwon:linkify:4.6.2")
//    }

//    -build.gradle(KitaHack2025)-
// Top-level build file where you can add configuration options common to all sub-projects/modules.
//buildscript {
//    dependencies {
//        classpath 'com.android.tools.build:gradle:8.8.0'
//        classpath 'com.google.gms:google-services:4.4.1'
//    }
//}
//
//    plugins {
//        id 'com.android.application' version '8.2.2' apply false
//        id 'com.google.gms.google-services' version '4.4.2' apply false
//    }

}
