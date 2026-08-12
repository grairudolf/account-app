# Complete Guide: Setting Up Sign-In & Account Creation

This document provides a step-by-step roadmap for configuring **Firebase Authentication** and **Google Sign-In** for your application.

---

## 1. Prerequisites & Required Tools

To enable cloud user authentication and account synchronization across devices, you will need:

1. **A Google Account**: Access to [Firebase Console](https://console.firebase.google.com/) and [Google Cloud Console](https://console.cloud.google.com/).
2. **Android Keystore SHA-1 Fingerprint**: Required for Google Sign-In verification.
3. **`google-services.json` File**: Firebase configuration file for your app's package name (`com.aistudio.cmfiaccountability.app`).

---

## 2. Step-by-Step Setup Instructions

### Step 1: Create a Firebase Project
1. Navigate to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** (or **Create a project**).
3. Enter your project name (e.g., `CMFI Accountability`) and choose whether to enable Google Analytics.
4. Click **Create project** and wait for provision completion.

---

### Step 2: Register Your Android App in Firebase
1. In your Firebase Project Overview dashboard, click the **Android icon** (`+ Add app`).
2. Enter your **Android package name**:
   - `com.aistudio.cmfiaccountability.app` *(Matches your `app/build.gradle.kts` `applicationId`)*
3. Enter an App nickname (e.g., `CMFI Accountability Mobile`).
4. **Get your SHA-1 fingerprint**:
   Run the following command in your terminal/Gradle wrapper:
   ```bash
   ./gradlew signingReport
   ```
   *Copy the SHA-1 value for `debug` / `release` variant and paste it into the **Debug signing certificate SHA-1** field in Firebase.*
5. Click **Register app**.

---

### Step 3: Download and Add `google-services.json`
1. Download the generated `google-services.json` configuration file from Firebase.
2. Place the file directly inside your app module root directory:
   ```
   app/google-services.json
   ```

---

### Step 4: Enable Authentication Providers in Firebase
1. In the Firebase Console left menu, expand **Build** and select [Authentication](https://console.firebase.google.com/u/0/project/_/authentication).
2. Click **Get started**.
3. Go to the **Sign-in method** tab and configure:
   - **Email/Password**: Enable and click **Save**.
   - **Google**: Enable, set your project support email, and click **Save**.
     *(Note down the Web Client ID generated under Web SDK configuration)*.

---

### Step 5: Configure Gradle Dependencies
Your project already includes Google Play Services and Firebase dependencies in `gradle/libs.versions.toml`. Ensure the Google Services Gradle plugin is applied in `app/build.gradle.kts`:

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.google.services) apply false
}

// App build.gradle.kts
plugins {
    alias(libs.plugins.google.services)
}

dependencies {
    // Firebase Authentication & Credentials Manager
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
}
```

---

## 3. Account Migration & Data Sync Flow

When a user switches from **Guest Mode** to a registered account:
1. The app preserves local Room database entries created as a guest.
2. `AuthViewModel.signInWithAccount(id, name, email, migrateLocalData = true)` automatically re-assigns all local `userId` fields from `guest_user` to the new authenticated user ID.
3. Offline data stays intact and seamlessly syncs to the cloud backend.

---

## 4. Useful Direct Links

- [Firebase Console](https://console.firebase.google.com/)
- [Google Cloud Credentials Manager](https://console.cloud.google.com/apis/credentials)
- [Android Credential Manager Guide](https://developer.android.com/identity/sign-in/credential-manager)
- [Firebase Auth Android SDK Reference](https://firebase.google.com/docs/auth/android/start)
