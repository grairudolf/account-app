package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.NotificationEntity
import com.example.data.repositories.AccountabilityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = accountabilityRepository.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = accountabilityRepository.unreadNotificationCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markAllAsRead() {
        viewModelScope.launch {
            accountabilityRepository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            accountabilityRepository.clearAllNotifications()
        }
    }

    fun triggerTestNotification(context: Context) {
        viewModelScope.launch {
            accountabilityRepository.logNotification(
                context = context,
                title = "Test Spiritual Notification",
                message = "System push notification working properly!",
                type = "SYSTEM"
            )
        }
    }
}
