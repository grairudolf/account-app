package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // "guest_user" or google/email UID
    val fullName: String,
    val email: String,
    val profileImageUri: String? = null,
    val localAssembly: String = "",
    val discipleMaker: String = "",
    val phoneNumber: String = "",
    val language: String = "en", // "en" or "fr"
    val themeMode: String = "LIGHT", // "LIGHT", "DARK", "SYSTEM"
    val conversionDate: String = "", // e.g. "2021-03-15"
    val accountabilityDays: String = "MON,TUE,WED,THU,FRI,SAT,SUN", // comma separated days
    val isGuest: Boolean = true,
    val syncStatus: String = "SYNCED",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)
