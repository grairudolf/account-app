package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.CustomDomainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomDomainDao {
    @Query("SELECT * FROM custom_domains ORDER BY createdAtMs ASC")
    fun getAllCustomDomainsFlow(): Flow<List<CustomDomainEntity>>

    @Query("SELECT * FROM custom_domains")
    suspend fun getAllDomainsList(): List<CustomDomainEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomDomain(domain: CustomDomainEntity)

    @Query("UPDATE custom_domains SET userId = :newUserId")
    suspend fun migrateUserCustomDomains(newUserId: String)

    @Query("DELETE FROM custom_domains WHERE id = :id")
    suspend fun deleteCustomDomainById(id: String)
}
