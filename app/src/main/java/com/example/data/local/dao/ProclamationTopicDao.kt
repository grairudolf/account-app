package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.ProclamationTopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProclamationTopicDao {

    @Query("SELECT * FROM proclamation_topics WHERE userId = :userId ORDER BY updatedAtMs DESC")
    fun getTopicsForUserFlow(userId: String): Flow<List<ProclamationTopicEntity>>

    @Query("SELECT * FROM proclamation_topics WHERE userId = :userId ORDER BY updatedAtMs DESC")
    suspend fun getTopicsForUser(userId: String): List<ProclamationTopicEntity>

    @Query("SELECT * FROM proclamation_topics WHERE id = :id")
    suspend fun getTopicById(id: String): ProclamationTopicEntity?

    @Query("SELECT * FROM proclamation_topics WHERE userId = :userId AND LOWER(topic) = LOWER(:topic) LIMIT 1")
    suspend fun findTopicByName(userId: String, topic: String): ProclamationTopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTopic(topic: ProclamationTopicEntity)

    @Delete
    suspend fun deleteTopic(topic: ProclamationTopicEntity)

    @Query("DELETE FROM proclamation_topics WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("UPDATE proclamation_topics SET userId = :newUserId")
    suspend fun migrateUserTopics(newUserId: String)
}
