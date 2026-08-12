package com.example.data.repositories

import com.example.core.localization.AppLanguage
import com.example.data.local.dao.UserDao
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    val currentUserFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()

    val currentLanguageFlow: Flow<AppLanguage> = userDao.getCurrentUserFlow().map { user ->
        val langCode = user?.language?.lowercase() ?: java.util.Locale.getDefault().language.lowercase()
        when {
            langCode.startsWith("fr") -> AppLanguage.FRENCH
            langCode.startsWith("es") -> AppLanguage.SPANISH
            langCode.startsWith("pt") -> AppLanguage.PORTUGUESE
            langCode.startsWith("sw") -> AppLanguage.SWAHILI
            langCode.startsWith("ar") -> AppLanguage.ARABIC
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
    }

    suspend fun updateLanguage(language: AppLanguage) {
        val current = getOrCreateGuestUser()
        val updated = current.copy(
            language = language.code,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(updated)
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        val current = getOrCreateGuestUser()
        val updated = current.copy(
            themeMode = themeMode.name,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(updated)
    }

    suspend fun signInAsGuest() {
        getOrCreateGuestUser()
    }

    suspend fun signInWithAccount(id: String, fullName: String, email: String) {
        val current = getOrCreateGuestUser()
        val updated = current.copy(
            id = id,
            fullName = if (fullName.isNotBlank()) fullName else current.fullName,
            email = email,
            isGuest = false,
            updatedAtMs = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(updated)
    }

    suspend fun signOut() {
        userDao.clearUserTable()
        getOrCreateGuestUser() // reset to guest
    }
}
