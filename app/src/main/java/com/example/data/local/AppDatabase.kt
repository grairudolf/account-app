package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        AccountabilityEntryEntity::class,
        GoalEntity::class,
        TimerSessionEntity::class,
        CustomDomainEntity::class,
        ReminderEntity::class,
        ReportRecordEntity::class,
        NotificationEntity::class,
        ProclamationTopicEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun entryDao(): EntryDao
    abstract fun goalDao(): GoalDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun customDomainDao(): CustomDomainDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reportDao(): ReportDao
    abstract fun notificationDao(): NotificationDao
    abstract fun proclamationTopicDao(): ProclamationTopicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cmfi_accountability.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
