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
import kotlinx.coroutines.launch

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
        val fbUser = if (context != null && com.example.services.auth.FirebaseAuthHelper.isFirebaseAvailable(context)) {
            com.example.services.auth.FirebaseAuthHelper.getCurrentUser()
        } else null

        val existing = userDao.getCurrentUser()

        if (fbUser != null) {
            val fbUid = fbUser.uid
            val fbEmail = fbUser.email ?: ""
            val fbName = fbUser.displayName?.ifBlank { null }
                ?: if (fbEmail.isNotBlank()) fbEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() } else "Disciple"
            val fbPhoto = fbUser.photoUrl?.toString()

            if (existing != null && !existing.isGuest && existing.id == fbUid) {
                // Background sync to pull any updates from other devices
                if (context != null) {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getInstance(context)
                            FirestoreSyncManager.performFullSync(context, db, fbUid)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                return existing
            }

            // Sync with Firebase Auth user
            val updated = (existing ?: UserEntity(
                id = fbUid,
                fullName = fbName,
                email = fbEmail,
                isGuest = false
            )).copy(
                id = fbUid,
                fullName = if (existing?.fullName?.isNotBlank() == true && existing.fullName != "Disciple") existing.fullName else fbName,
                email = fbEmail,
                profileImageUri = fbPhoto ?: existing?.profileImageUri,
                isGuest = false,
                updatedAtMs = System.currentTimeMillis()
            )
            userDao.clearUserTable()
            userDao.insertOrUpdateUser(updated)
            hasCompletedAuthPrompt = true

            if (context != null) {
                try {
                    val db = AppDatabase.getInstance(context)
                    db.entryDao().migrateUserEntries(fbUid)
                    db.goalDao().migrateUserGoals(fbUid)
                    db.discipleDao().migrateUserDisciples(fbUid)
                    db.customDomainDao().migrateUserCustomDomains(fbUid)
                    db.proclamationTopicDao().migrateUserTopics(fbUid)
                    FirestoreSyncManager.restoreUserDataFromCloud(context, db, fbUid)
                    val fresh = userDao.getCurrentUser() ?: updated
                    FirestoreSyncManager.syncUserProfile(context, fresh)
                    FirestoreSyncManager.syncAllLocalDataToCloud(context, db, fbUid)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return updated
        }

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
        val current = userDao.getCurrentUser()
        val finalName = if (fullName.isNotBlank() && fullName != "Disciple") {
            fullName
        } else if (current?.fullName?.isNotBlank() == true && current.fullName != "Disciple") {
            current.fullName
        } else if (email.isNotBlank()) {
            email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        } else {
            "Disciple"
        }

        val finalImageUri = if (!profileImageUri.isNullOrBlank()) {
            profileImageUri
        } else {
            current?.profileImageUri
        }

        val finalAssembly = if (localAssembly.isNotBlank()) {
            localAssembly
        } else {
            current?.localAssembly ?: ""
        }

        val updated = (current ?: UserEntity(id = id, fullName = finalName, email = email)).copy(
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
        // Also upload all local guest entries so everything is backed up to Firestore
        if (context != null) {
            try {
                val db = AppDatabase.getInstance(context)
                db.entryDao().migrateUserEntries(id)
                db.goalDao().migrateUserGoals(id)
                db.discipleDao().migrateUserDisciples(id)
                db.customDomainDao().migrateUserCustomDomains(id)
                db.proclamationTopicDao().migrateUserTopics(id)
                db.reportDao().migrateUserReports(id)

                FirestoreSyncManager.performFullSync(context, db, id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val syncProgress: kotlinx.coroutines.flow.StateFlow<com.example.services.sync.SyncProgress> =
        com.example.services.sync.FirestoreSyncManager.syncProgressFlow

    suspend fun syncAllCloudData(): Boolean {
        if (context == null) return false
        val user = userDao.getCurrentUser() ?: return false
        if (user.isGuest || user.id.isBlank() || user.id == "guest_user") return false
        return try {
            val db = AppDatabase.getInstance(context)
            FirestoreSyncManager.performFullSync(context, db, user.id)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signOut() {
        hasCompletedAuthPrompt = false
        com.example.services.auth.FirebaseAuthHelper.signOut()
        userDao.clearUserTable()
        val defaultGuest = UserEntity(
            id = "guest_user",
            fullName = "Disciple",
            email = "",
            isGuest = true,
            language = "en",
            themeMode = "LIGHT"
        )
        userDao.insertOrUpdateUser(defaultGuest)
    }
}
