package com.oliinyk.yaroslav.easyreads.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.oliinyk.yaroslav.easyreads.data.local.entety.ReadingGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingGoalDao {

    @Query("SELECT * FROM reading_goals WHERE year = :year")
    fun getByYear(year: Int): Flow<ReadingGoalEntity?>

    @Insert
    suspend fun insert(readingGoalEntity: ReadingGoalEntity)

    @Update
    suspend fun update(readingGoalEntity: ReadingGoalEntity)
}