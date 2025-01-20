package com.oliinyk.yaroslav.easyreads.data.repository

import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSessionRecordStatusType
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadTimeCounterRepository
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadTimeCounterRepositoryImpl @Inject constructor(
    private val readingSessionRepository: ReadingSessionRepository
) : ReadTimeCounterRepository {

    private val coroutineScope: CoroutineScope = GlobalScope
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    private var timeCounterJob: Job? = null
    private var _readingSession: ReadingSession = ReadingSession()

    private var onTick: (Date) -> Unit = { date ->
        if (timeCounterJob?.isActive == true) {
            val timeDifference = date.time - _readingSession.updatedDate.time
            _readingSession = _readingSession.copy(
                updatedDate = date,
                readTimeInMilliseconds = _readingSession.readTimeInMilliseconds + timeDifference
            )
            readingSessionRepository.update(_readingSession)
        }
    }

    private fun updateReadingSession(readingSession: ReadingSession) {
        timeCounterJob?.cancel()

        _readingSession = if (_readingSession.recordStatus == ReadingSessionRecordStatusType.STARTED) {
            readingSession.copy()
        } else {
            readingSession.copy(
                updatedDate = Date()
            )
        }
    }

    override fun getReadTimeInMilliseconds(): Long {
        return _readingSession.readTimeInMilliseconds
    }

    override fun start(bookId: UUID, pageCurrent: Int) {
        timeCounterJob?.cancel()

        coroutineScope.launch(coroutineDispatcher) {
            readingSessionRepository.getLastUnfinishedByBookId(bookId)
                .take(1)
                .collect { readingSession ->
                    if (readingSession == null) {
                        _readingSession = ReadingSession(
                                bookId = bookId,
                                startPage = pageCurrent
                            )
                        readingSessionRepository.insert(_readingSession)
                    } else {
                        updateReadingSession(readingSession)
                    }
                }
        }

        timeCounterJob = coroutineScope.launch(coroutineDispatcher) {
            createTimeCounter().collect { date ->
                onTick(date)
            }
        }
    }

    override fun resume() {
        timeCounterJob?.cancel()

        _readingSession = _readingSession.copy(
            updatedDate = Date(),
            recordStatus = ReadingSessionRecordStatusType.STARTED
        )
        readingSessionRepository.update(_readingSession)

        timeCounterJob = coroutineScope.launch(coroutineDispatcher) {
            createTimeCounter().collect { date ->
                onTick(date)
            }
        }
    }

    override fun pause() {
        timeCounterJob?.cancel()

        _readingSession = _readingSession.copy(
            recordStatus = ReadingSessionRecordStatusType.PAUSED
        )
        readingSessionRepository.update(_readingSession)
    }

    override fun stop() {
        timeCounterJob?.cancel()
        timeCounterJob = null
        _readingSession = ReadingSession()
    }

    private fun createTimeCounter(): Flow<Date> = flow {
        while (true) {
            delay(AppConstants.MILLISECONDS_IN_ONE_SECOND)
            emit(Date())
        }
    }
}