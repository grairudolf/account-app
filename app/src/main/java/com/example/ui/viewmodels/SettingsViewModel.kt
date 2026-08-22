package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.UserRepository
import com.example.services.notifications.ReminderManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentLanguage: StateFlow<AppLanguage> = userRepository.currentLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    val currentTheme: StateFlow<ThemeMode> = userRepository.currentThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.LIGHT)

    val reminders: StateFlow<List<ReminderEntity>> = accountabilityRepository.remindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            userRepository.updateLanguage(language)
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userRepository.updateThemeMode(themeMode)
        }
    }

    fun updateProfileImage(uri: String) {
        viewModelScope.launch {
            userRepository.updateProfileImage(uri)
        }
    }

    fun updateProfile(
        fullName: String,
        email: String,
        localAssembly: String,
        discipleMaker: String,
        phone: String,
        conversionDate: String = "",
        accountabilityDays: String = "MON,TUE,WED,THU,FRI,SAT,SUN"
    ) {
        viewModelScope.launch {
            userRepository.updateProfile(fullName, email, localAssembly, discipleMaker, phone, conversionDate, accountabilityDays)
        }
    }

    fun addOrUpdateReminder(context: Context, domainId: String, title: String, message: String, hour: Int, minute: Int, id: String? = null) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val reminder = ReminderEntity(
                id = id ?: UUID.randomUUID().toString(),
                userId = user.id,
                domainId = domainId,
                title = title,
                message = message,
                hour = hour,
                minute = minute,
                isEnabled = true
            )
            accountabilityRepository.saveReminder(reminder)
            ReminderManager.scheduleReminder(context, reminder)
        }
    }

    fun toggleReminder(context: Context, reminder: ReminderEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = isEnabled)
            accountabilityRepository.saveReminder(updated)
            if (isEnabled) {
                ReminderManager.scheduleReminder(context, updated)
            } else {
                ReminderManager.cancelReminder(context, updated.id)
            }
        }
    }

    fun deleteReminder(context: Context, id: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteReminder(id)
            ReminderManager.cancelReminder(context, id)
        }
    }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun syncCloudData(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            val success = userRepository.syncAllCloudData()
            _isSyncing.value = false
            onComplete?.invoke(success)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            accountabilityRepository.clearAllData()
        }
    }
}
