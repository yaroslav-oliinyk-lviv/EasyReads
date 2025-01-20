package com.oliinyk.yaroslav.easyreads.presentation.reading_session.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReadingSessionListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingSessionRepository: ReadingSessionRepository
) : ViewModel() {

    private val _stateUi = MutableStateFlow(ReadingSessionListUiState())
    val stateUi
        get() = _stateUi.asStateFlow()

    fun loadReadingSessionsByBookId(book: Book) {
        _stateUi.update { it.copy(book = book) }

        viewModelScope.launch {
            readingSessionRepository.getAllByBookId(bookId = book.id).collect { readingSessions ->
                _stateUi.update { it.copy(readingSessions = readingSessions) }
            }
        }
    }

    fun addReadingSession(readingSession: ReadingSession) {
        stateUi.value.book?.let { book ->
            bookRepository.update(
                book.copy(
                    pageCurrent = readingSession.endPage,
                    updatedDate = Date()
                )
            )

            readingSessionRepository.insert(
                readingSession.copy(
                    bookId = book.id
                )
            )
        }
    }

    fun updateReadingSession(readingSession: ReadingSession) {
        stateUi.value.book?.let { book ->

            if (_stateUi.value.readingSessions[0].id == readingSession.id) {
                bookRepository.update(
                    book.copy(pageCurrent = readingSession.endPage)
                )
            }

            readingSessionRepository.update(readingSession)
        }
    }

    fun remove(readingSession: ReadingSession) {
        stateUi.value.book?.let { book ->

            if (book.pageCurrent == readingSession.endPage) {
                bookRepository.update(
                    book.copy(pageCurrent = readingSession.startPage)
                )
            }

            readingSessionRepository.remove(readingSession)
        }
    }
}

data class ReadingSessionListUiState(
    val book: Book? = null,
    val readingSessions: List<ReadingSession> = emptyList()
)