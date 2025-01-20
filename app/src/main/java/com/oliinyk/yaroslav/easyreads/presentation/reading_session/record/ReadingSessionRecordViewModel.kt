package com.oliinyk.yaroslav.easyreads.presentation.reading_session.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSessionRecordStatusType
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.NoteRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import com.oliinyk.yaroslav.easyreads.domain.service.ReadTimeCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReadingSessionRecordViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val noteRepository: NoteRepository,
    private val readingSessionRepository: ReadingSessionRepository
) : ViewModel() {

    private val _stateUi = MutableStateFlow(ReadingSessionRecordStateUi())
    val stateUi
        get() = _stateUi.asStateFlow()

    val currentReadingSession
        get() = stateUi.value.readingSession

    val currentBook: Book
        get() = checkNotNull(stateUi.value.book)

    fun loadLastUnfinishedByBookId(book: Book) {
        _stateUi.update { it.copy(book = book) }
        viewModelScope.launch {
            readingSessionRepository.getLastUnfinishedByBookId(book.id).collect { readingSessionFromDB ->
                readingSessionFromDB?.let {
                    _stateUi.update { it.copy(readingSession = readingSessionFromDB) }
                }
            }
        }
    }

    fun updateStateUi(onUpdate: (ReadingSessionRecordStateUi) -> ReadingSessionRecordStateUi) {
        _stateUi.update { onUpdate(it) }
    }

    fun resumeOrPause(onUpdate: (ReadTimeCounterService.Actions) -> Unit) {
        _stateUi.value.readingSession?.let { readingSession ->
            when (readingSession.recordStatus) {
                ReadingSessionRecordStatusType.PAUSED -> {
                    onUpdate(ReadTimeCounterService.Actions.RESUME)
                }

                ReadingSessionRecordStatusType.STARTED -> {
                    onUpdate(ReadTimeCounterService.Actions.PAUSE)
                }

                else -> {
                    onUpdate(ReadTimeCounterService.Actions.PAUSE)
                }
            }
        }
    }

    fun removeUnfinishedReadingSession() {
        _stateUi.value.readingSession?.let { readingSession ->
            readingSessionRepository.remove(readingSession)
        }
        updateStateUi { it.copy(readingSession = null) }
    }

    fun save(readingSession: ReadingSession) {
        stateUi.value.book?.let { book ->
            bookRepository.update(
                book.copy(
                    pageCurrent = readingSession.endPage,
                    updatedDate = Date()
                )
            )

            readingSessionRepository.update(
                readingSession.copy(recordStatus = ReadingSessionRecordStatusType.FINISHED)
            )
        }
        updateStateUi { it.copy(readingSession = null) }
    }

    fun addNote(note: Note) {
        noteRepository.insert(note)
    }
}

data class ReadingSessionRecordStateUi(
    val book: Book? = null,
    val readingSession: ReadingSession? = null
)