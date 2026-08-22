package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AccountabilityEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM accountability_entries ORDER BY timestampMs DESC")
    fun getAllEntriesFlow(): Flow<List<AccountabilityEntryEntity>>

    @Query("SELECT * FROM accountability_entries WHERE dateIso = :dateIso ORDER BY timestampMs DESC")
    fun getEntriesByDateFlow(dateIso: String): Flow<List<AccountabilityEntryEntity>>

    @Query("SELECT * FROM accountability_entries WHERE dateIso = :dateIso")
    suspend fun getEntriesByDate(dateIso: String): List<AccountabilityEntryEntity>

    @Query("SELECT * FROM accountability_entries WHERE domainId = :domainId ORDER BY timestampMs DESC")
    fun getEntriesByDomainFlow(domainId: String): Flow<List<AccountabilityEntryEntity>>

    @Query("SELECT * FROM accountability_entries WHERE id = :id")
    suspend fun getEntryById(id: String): AccountabilityEntryEntity?

    @Query("SELECT * FROM accountability_entries")
    suspend fun getAllEntriesList(): List<AccountabilityEntryEntity>

    @Query("SELECT * FROM accountability_entries WHERE timestampMs >= :startMs AND timestampMs <= :endMs ORDER BY timestampMs DESC")
    suspend fun getEntriesInRange(startMs: Long, endMs: Long): List<AccountabilityEntryEntity>

    @Query("SELECT * FROM accountability_entries WHERE (notes LIKE '%' || :query || '%' OR reflection LIKE '%' || :query || '%' OR bibleBook LIKE '%' || :query || '%' OR bookTitle LIKE '%' || :query || '%') ORDER BY timestampMs DESC")
    fun searchEntriesFlow(query: String): Flow<List<AccountabilityEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEntry(entry: AccountabilityEntryEntity)

    @Query("DELETE FROM accountability_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM accountability_entries")
    suspend fun clearAllEntries()

    @Query("UPDATE accountability_entries SET userId = :newUserId")
    suspend fun migrateUserEntries(newUserId: String)
}
