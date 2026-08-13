package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.UserRepository
import com.example.services.timer.TimerServiceManager

class ViewModelFactory private constructor(
    private val context: Context,
    private val userRepository: UserRepository,
    private val accountabilityRepository: AccountabilityRepository,
    private val timerServiceManager: TimerServiceManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(userRepository, accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(userRepository, accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(DomainsViewModel::class.java) -> {
                DomainsViewModel(accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(TimerViewModel::class.java) -> {
                TimerViewModel(context, timerServiceManager, accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(EntryViewModel::class.java) -> {
                EntryViewModel(accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(GoalsViewModel::class.java) -> {
                GoalsViewModel(accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                CalendarViewModel(accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                StatisticsViewModel(accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> {
                ReportsViewModel(userRepository, accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(userRepository, accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> {
                NotificationsViewModel(accountabilityRepository) as T
            }
            modelClass.isAssignableFrom(ProclamationViewModel::class.java) -> {
                ProclamationViewModel(context, userRepository, accountabilityRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context.applicationContext)
                val userRepo = UserRepository(db.userDao())
                val accRepo = AccountabilityRepository(
                    db.entryDao(),
                    db.goalDao(),
                    db.customDomainDao(),
                    db.reminderDao(),
                    db.reportDao(),
                    db.notificationDao(),
                    db.proclamationTopicDao()
                )
                val timerMgr = TimerServiceManager(db.timerSessionDao())
                val instance = ViewModelFactory(context.applicationContext, userRepo, accRepo, timerMgr)
                INSTANCE = instance
                instance
            }
        }
    }
}
