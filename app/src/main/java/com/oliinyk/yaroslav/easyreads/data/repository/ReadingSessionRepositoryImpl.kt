package com.oliinyk.yaroslav.easyreads.data.repository

import com.oliinyk.yaroslav.easyreads.data.local.dao.ReadingSessionDao
import com.oliinyk.yaroslav.easyreads.data.local.entety.toModel
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.model.toEntity
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingSessionRepositoryImpl @Inject constructor(
    private val readingSessionDao: ReadingSessionDao
) : ReadingSessionRepository {

    private val coroutineScope: CoroutineScope = GlobalScope
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getAllByBookId(bookId: UUID): Flow<List<ReadingSession>> {
        return readingSessionDao.getAllByBookId(bookId).map { entities ->
            entities.map {
                it.toModel()
            }
        }
    }

    override fun getLastUnfinishedByBookId(bookId: UUID): Flow<ReadingSession?> {
        return readingSessionDao.getLastUnfinishedByBookId(bookId).map {
            it?.toModel()
        }.distinctUntilChanged()
    }

    override fun insert(readingSession: ReadingSession) {
        coroutineScope.launch(coroutineDispatcher) {
            readingSessionDao.insert(readingSession.toEntity())
        }
    }

    override fun update(readingSession: ReadingSession) {
        coroutineScope.launch(coroutineDispatcher) {
            readingSessionDao.update(readingSession.toEntity())
        }
    }

    override fun remove(readingSession: ReadingSession) {
        coroutineScope.launch(coroutineDispatcher) {
            readingSessionDao.delete(readingSession.toEntity())
        }
    }

    override fun remove(readingSessions: List<ReadingSession>) {
        coroutineScope.launch(coroutineDispatcher) {
            readingSessionDao.delete(
                readingSessions.map { it.toEntity() }
            )
        }
    }
}