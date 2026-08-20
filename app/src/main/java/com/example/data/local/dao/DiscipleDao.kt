package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.DiscipleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscipleDao {

    @Query("SELECT * FROM disciples WHERE userId = :userId ORDER BY name ASC")
    fun getAllDisciples(userId: String): Flow<List<DiscipleEntity>>

    @Query("SELECT * FROM disciples WHERE id = :id LIMIT 1")
    suspend fun getDiscipleById(id: String): DiscipleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisciple(disciple: DiscipleEntity)

    @Update
    suspend fun updateDisciple(disciple: DiscipleEntity)

    @Delete
    suspend fun deleteDisciple(disciple: DiscipleEntity)

    @Query("DELETE FROM disciples WHERE id = :id")
    suspend fun deleteDiscipleById(id: String)
}
