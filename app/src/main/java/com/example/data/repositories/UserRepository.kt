package com.example.data.repositories

import android.content.Context
import com.example.core.localization.AppLanguage
import com.example.data.local.AppDatabase
import com.example.data.local.dao.UserDao
import com.example.data.local.entities.UserEntity
import com.example.services.sync.FirestoreSyncManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userDao: UserDao,
    private val context: Context? = null
) {
    private val prefs = context?.getSharedPreferences("cmfi_user_prefs", Context.MODE_PRIVATE)

    var hasCompletedAuthPrompt: Boolean
        get() = prefs?.getBoolean("has_completed_auth_prompt", false) ?: false
        set(value) {
            prefs?.edit()?.putBoolean("has_completed_auth_prompt", value)?.apply()
        }

    val currentUserFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()

    val currentLanguageFlow: Flow<AppLanguage> = userDao.getCurrentUserFlow().map { user ->
        val langCode = user?.language?.lowercase() ?: java.util.Locale.getDefault().language.lowercase()
        when {
            langCode.startsWith("fr") -> AppLanguage.FRENCH
            else -> AppLanguage.ENGLISH
        }
    }

    val currentThemeFlow: Flow<ThemeMode> = userDao.getCurrentUserFlow().map { user ->
        when (user?.themeMode) {
            "DARK" -> ThemeMode.DARK
            "SYSTEM" -> ThemeMode.SYSTEM
            else -> ThemeMode.LIGHT
        }
    }

    suspend fun getOrCreateGuestUser(): UserEntity {
        val existing = userDao.getCurrentUser()
        if (existing != null) return existing

        val sysLangCode = java.util.Locale.getDefault().language.lowercase()
        val systemLang = when {
            sysLangCode.startsWith("fr") -> "fr"
            sysLangCode.startsWith("es") -> "es"
            sysLangCode.startsWith("pt") -> "pt"
            sysLangCode.startsWith("sw") -> "sw"
            sysLangCode.startsWith("ar") -> "ar"
            else -> "en"
        }
        val defaultGuest = UserEntity(
            id = "guest_user",
            fullName = "Disciple",
            email = "",
            isGuest = true,
            language = systemLang,
            themeMode = "LIGHT"
        )
        userDao.insertOrUpdateUser(defaultGuest)
        return defaultGuest
    }

    suspend fun updateProfile(
        fullName: String,
        email: String,
        localAssembly: String,
        discipleMaker: String,
        phoneNumber: String,
        conversionDate: String = "",
        accountabilityDays: String = "MON,TUE,WED,THU,FRI,SAT,SUN"
    ) {
        val current = getOrCreateGuestUser()
        val updated = current.copy(
            fullName = fullName,
            email = email,
            localAssembly = localAssembly,
            discipleMaker = discipleMaker,
            phoneNumber = phoneNumber,
            conversionDate = conversionDate,
            accountabilityDays = accountabilityDays,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(updated)
        if (context != null && !updated.isGuest) {
            FirestoreSyncManager.syncUserProfile(context, updated)
        }
    }

    suspend fun updateProfileImage(uri: String) {
        val current = getOrCreateGuestUser()
        val updated = current.copy(
            profileImageUri = uri,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(updated)
        if (context != null && !updated.isGuest) {
            FirestoreSyncManager.syncUserProfile(context, updated)
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        val current = getOrCreateGuestUser()
        val updated = current.copy(
            language = language.code,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(updated)
        if (context != null && !updated.isGuest) {
            FirestoreSyncManager.syncUserProfile(context, updated)
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        val current = getOrCreateGuestUser()
        val updated = current.copy(
            themeMode = themeMode.name,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(updated)
        if (context != null && !updated.isGuest) {
            FirestoreSyncManager.syncUserProfile(context, updated)
        }
    }

    suspend fun signInAsGuest() {
        hasCompletedAuthPrompt = true
        getOrCreateGuestUser()
    }

    suspend fun signInWithAccount(
        id: String,
        fullName: String,
        email: String,
        profileImageUri: String? = null,
        localAssembly: String = ""
    ) {
        hasCompletedAuthPrompt = true
        val current = getOrCreateGuestUser()
        val finalName = if (fullName.isNotBlank() && fullName != "Disciple") {
            fullName
        } else if (current.fullName.isNotBlank() && current.fullName != "Disciple") {
            current.fullName
        } else {
            email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        }

        val finalImageUri = if (!profileImageUri.isNullOrBlank()) {
            profileImageUri
        } else {
            current.profileImageUri
        }

        val finalAssembly = if (localAssembly.isNotBlank()) {
            localAssembly
        } else {
            current.localAssembly
        }

        val updated = current.copy(
            id = id,
            fullName = finalName,
            email = email,
            profileImageUri = finalImageUri,
            localAssembly = finalAssembly,
            isGuest = false,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.clearUserTable()
        userDao.insertOrUpdateUser(updated)

        // Attempt cloud restore so user's existing goals, progress, streaks, reports, and disciples on that account are loaded!
        if (context != null) {
            try {
                FirestoreSyncManager.restoreUserDataFromCloud(context, AppDatabase.getInstance(context), id)
                // Backup current user profile if freshly updated
                val freshUser = userDao.getCurrentUser() ?: updated
                FirestoreSyncManager.syncUserProfile(context, freshUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun signOut() {
        hasCompletedAuthPrompt = false
        userDao.clearUserTable()
        getOrCreateGuestUser() // reset to guest
    }
}
