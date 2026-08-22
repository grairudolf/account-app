# Complete Firebase & Google Sign-In Setup Guide

This guide gives you all the exact credentials, certificate fingerprints, and step-by-step instructions needed to connect your CMFI Accountability app to Firebase.

---

## 1. App Identifiers & Certificate Fingerprints

When configuring Firebase and Google Cloud Console, copy and paste these exact values:

| Parameter | Value | Notes |
| :--- | :--- | :--- |
| **Android Package Name** (`applicationId`) | `com.aistudio.cmfiaccountability.app` | Defined in `app/build.gradle.kts` |
| **App Nickname** | `CMFI Accountability` | Descriptive name |
| **Debug SHA-1 Fingerprint** | `8F:51:33:D4:F5:75:39:78:38:74:01:9C:1B:BA:08:97:9B:A3:D3:21` | Required for Google Sign-In & Phone Auth |
| **Debug SHA-256 Fingerprint** | `01:90:26:0E:38:A1:9F:46:F7:05:0C:B3:8E:5B:B7:12:99:8A:85:CA:42:0D:EC:86:62:5A:77:89:13:9D:0C:67` | Required for App Check & OAuth verification |

---

## 2. Step-by-Step Setup Guide

### Step 1: Create / Open your Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** (or select your existing project).
3. Name your project (e.g., `CMFI Accountability`) and complete the creation steps.

---

### Step 2: Register the Android App
1. In the Project Overview dashboard, click the **Android icon** (`+ Add app`).
2. Fill in the fields:
   - **Android package name**: `com.aistudio.cmfiaccountability.app`
   - **App nickname**: `CMFI Accountability`
   - **Debug signing certificate SHA-1**: `8F:51:33:D4:F5:75:39:78:38:74:01:9C:1B:BA:08:97:9B:A3:D3:21`
3. Click **Register app**.

---

### Step 3: Download and Place `google-services.json`
1. Click **Download google-services.json**.
2. Place the downloaded file directly inside the `app/` directory of the project:
   ```
   app/
   ├── google-services.json   <--- PUT THE FILE HERE
   ├── build.gradle.kts
   └── src/
   ```

---

### Step 4: Enable Authentication Providers in Firebase Console
1. In the left navigation menu, go to **Build** > **Authentication**.
2. Click **Get Started** (if not already enabled).
3. Go to the **Sign-in method** tab:
   - **Email/Password**: Click on it, toggle **Enable**, and click **Save**.
   - **Google**: Click on it, toggle **Enable**, select your **Project support email**, and click **Save**.
4. Under **Project Settings** (gear icon) > **Your apps** > **Android apps**, click **Add fingerprint** and add both the **SHA-1** and **SHA-256** values listed in Section 1 above.

---

### Step 5: (Optional) Google Cloud OAuth Consent Screen
1. Go to the [Google Cloud Console Credentials](https://console.cloud.google.com/apis/credentials).
2. Ensure your OAuth consent screen is configured with User Type: **External** (or Internal for Workspace) and Support Email.
3. The Web Client ID generated automatically in Firebase will match the OAuth 2.0 Web Client in Google Cloud.

---

## 3. How the App Handles Authentication

- **Email & Password**: Creates live accounts via `FirebaseAuth.createUserWithEmailAndPassword()` and authenticates users via `FirebaseAuth.signInWithEmailAndPassword()`.
- **Google Sign-In**: Uses Android Jetpack `CredentialManager` to present the native one-tap Google account selector, validates the Google ID token with Firebase Auth, and authenticates the user.
- **Forgot Password**: Sends real password reset emails via `FirebaseAuth.sendPasswordResetEmail()`.
- **Guest / Offline Mode**: If offline or before `google-services.json` is added, users can continue as a Guest. When signing in or registering later, local discipline data automatically migrates to the registered account.
