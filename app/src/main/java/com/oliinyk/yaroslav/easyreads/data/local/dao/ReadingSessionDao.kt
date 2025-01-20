package com.oliinyk.yaroslav.easyreads.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.oliinyk.yaroslav.easyreads.data.local.entety.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ReadingSessionDao {

    @Query("SELECT * FROM reading_sessions WHERE book_id = :bookId ORDER BY started_date DESC")
    fun getAllByBookId(bookId: UUID): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions" +
            " WHERE book_id = :bookId AND record_status <> 'FINISHED'" +
            " ORDER BY started_date DESC" +
            " LIMIT 1")
    fun getLastUnfinishedByBookId(bookId: UUID): Flow<ReadingSessionEntity?>

    @Insert
    suspend fun insert(readingSessionEntity: ReadingSessionEntity)

    @Update
    suspend fun update(readingSessionEntity: ReadingSessionEntity)

    @Delete
    suspend fun delete(readingSessionEntity: ReadingSessionEntity)

    @Delete
    suspend fun delete(readingSessionEntities: List<ReadingSessionEntity>)
}