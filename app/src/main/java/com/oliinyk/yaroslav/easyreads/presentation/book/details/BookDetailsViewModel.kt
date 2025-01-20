package com.oliinyk.yaroslav.easyreads.presentation.book.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.NoteRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val noteRepository: NoteRepository,
    private val readingSessionRepository: ReadingSessionRepository
) : ViewModel() {

    private val _stateUi: MutableStateFlow<BookDetailsUiState> =
        MutableStateFlow(BookDetailsUiState())
    val stateUi: StateFlow<BookDetailsUiState>
        get() = _stateUi.asStateFlow()

    fun loadBookById(bookId: UUID) {
        viewModelScope.launch {
            bookRepository.getById(bookId).collect { bookCollected ->
                bookCollected?.let { book ->
                    _stateUi.update { oldUiState ->
                        oldUiState.copy(book = book)
                    }
                }
            }
        }
        viewModelScope.launch {
            noteRepository.getAllByBookId(bookId).collect { notes ->
                _stateUi.update { it.copy(notes = notes) }
            }
        }
        viewModelScope.launch {
            readingSessionRepository.getAllByBookId(bookId).collect { readingSessions ->
                _stateUi.update { it.copy(readingSessions = readingSessions) }
            }
        }
    }

    fun getCurrentBook(): Book = stateUi.value.book

    fun removeCurrentBook() {
        bookRepository.remove(stateUi.value.book)
        noteRepository.remove(stateUi.value.notes)
        readingSessionRepository.remove(stateUi.value.readingSessions)
    }

    fun updateStateUi(onUpdate: (BookDetailsUiState) -> BookDetailsUiState) {
        _stateUi.update {
            onUpdate(it)
        }
    }

    fun getNotes(): List<Note> {
        return stateUi.value.notes
    }

    fun addNote(note: Note) {
        noteRepository.insert(
            note.copy(bookId = stateUi.value.book.id)
        )
    }

    fun updateNote(note: Note) {
        noteRepository.update(note)
    }

    fun getReadingSessions(): List<ReadingSession> {
        return stateUi.value.readingSessions
    }

    fun addReadingSession(readingSession: ReadingSession) {
        bookRepository.update(
            stateUi.value.book.copy(pageCurrent = readingSession.endPage)
        )

        readingSessionRepository.insert(
            readingSession.copy(
                bookId = stateUi.value.book.id
            )
        )
    }

    fun updateReadingSession(readingSession: ReadingSession) {
        bookRepository.update(
            stateUi.value.book.copy(pageCurrent = readingSession.endPage)
        )

        readingSessionRepository.update(readingSession)
    }
}

data class BookDetailsUiState(
    val book: Book = Book(),
    val notes: List<Note> = emptyList(),
    val readingSessions: List<ReadingSession> = emptyList()
)