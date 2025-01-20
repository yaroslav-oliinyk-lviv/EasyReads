package com.oliinyk.yaroslav.easyreads.domain.repository

import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ReadingSessionRepository {

    fun getAllByBookId(bookId: UUID): Flow<List<ReadingSession>>

    fun getLastUnfinishedByBookId(bookId: UUID): Flow<ReadingSession?>

    fun insert(readingSession: ReadingSession)

    fun update(readingSession: ReadingSession)

    fun remove(readingSession: ReadingSession)
    fun remove(readingSessions: List<ReadingSession>)
}