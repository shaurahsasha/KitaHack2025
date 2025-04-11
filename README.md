# AegisAqua Android App

AegisAqua is a smart donation and sustainability platform designed to facilitate the sharing of essential and relief resources. Built for community resilience, AegisAqua connects donors and recipients with seamless donation flows, real-time assistance, and support for community-driven initiatives.

## Features

### Core Features 
- Essential and relief item donation management
- Request system for individuals and communities in need
- Community events and outreach programs
- Real-time chatbot assistant powered by Gemini AI
- User profile and history management
- Image upload and management
- Geolocation-based services
- Google integration

### User Interface
- Modern material design 3 implementation
- Clean, intuitive layouts with ConstraintLayout
- Bottom navigations: Home, Shelter, Action, Profile
- Image display and caching using Glide
- Custom loading animations and pop-ups
- Accessibility support
- Typography using custom Poppins fonts

## Tech Stack

### Core Technologies
- Language: Java & Kotlin
- Platform: Android(min SDK 30, target SDK 34)
- Build System: Gradle with Kotlin DSL

### Firebase Integration
- Authentication (Email/Password, Google Sign-in)
- Cloud Firestore for data storage
- Firebase Storage for media
- Real-time data synchronization

### Key Dependencies
```kotlin
dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging:24.1.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-tasks:18.2.0")

    //Firebase Storage
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-storage:21.0.1")

    // UI Components
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    
    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("org.json:json:20210307")
    
    // AI Integration
    implementation("com.google.ai.client.generativeai:generativeai:0.1.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

}
```

## Project Setup

### Prerequisites
- Android Studio Meerket or newer
- JDK 11 or higher
- Android SDK with minimum API level 30
- Google Services configuration
- Gemini API key for AI features

### Building the Project 
1. Clone the repository
```bash
git clone https://github.com/shaurahsasha/KitaHack2025.git
```

2. Add required API keys:
   - Create a 'local.properties' file in the project root
   - Add your Gemini API key:
     ```properties
     GEMINI_API_KEY=your_api_key_here
     ```
3. Add Firebase configuration:
  - Add your 'google-services.json' to the app directory
  - Configure Firebase services in the Firebase Console
    
5. Build and run:
   - Open in Android Studio
   - Sync Gradle files
   - Run on an emulator or physical device (API 30+)

## Architecture

### MVVM Pattern
- View layer: Activities and Fragments
- ViewModel: Data handling and business logic
- Repository: Data access and management
- Model: Data classes and entities

### Key Components
- 'AegisAquaChatbot': AI-powered chat assistant
- 'OfferEssentialRepository': Handles user management
- 'UserRepo': Handles user management
- 'SheltersRepo': Manages community shelters

### Security
- Firebase Authentication with multiple providers
- Secure file storage with Firebase Storage
- Protected database access rules
- Encrypted user data
- Safe location sharing
- Runtime permissions handling

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for more information.

## Contact

Project Link: [https://github.com/shaurahsasha/KitaHack2025]

## Acknowledgement
- Material Design Components
- Firebase Platform
- Google AI (Gemini)
- Android Jetpack Libraries
