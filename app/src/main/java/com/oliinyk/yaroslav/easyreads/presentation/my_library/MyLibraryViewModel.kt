package com.oliinyk.yaroslav.easyreads.presentation.my_library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType.*
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MyLibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _stateUi: MutableStateFlow<MyLibraryUiState> =
        MutableStateFlow(MyLibraryUiState())
    val stateUi: StateFlow<MyLibraryUiState>
        get() = _stateUi.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.getAll().collectLatest { books ->
                val currentYearFinishedBooks: List<Book> = books.filter {
                    it.isFinished && (it.finishedDate != null) && (it.finishedDate.year == Date().year)
                }
                _stateUi.update {
                    it.copy(
                        finishedCount = books.filter { it.shelve == FINISHED }.count(),
                        readingCount = books.filter { it.shelve == READING }.count(),
                        wantToReadCount = books.filter { it.shelve == WANT_TO_READ }.count(),
                        allCount = books.size,
                        currentYearFinishedBooksCount = currentYearFinishedBooks.size
                    )
                }
            }
        }
    }
}

data class MyLibraryUiState(
    val finishedCount: Int = 0,
    val readingCount: Int = 0,
    val wantToReadCount: Int = 0,
    val allCount: Int = 0,
    val currentYearFinishedBooksCount: Int = 0,
    val readingGoal: Int = 12 //TODO:  year goals read from DB
)