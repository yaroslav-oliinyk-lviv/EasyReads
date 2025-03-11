package com.oliinyk.yaroslav.easyreads.presentation.reading_goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingGoal
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingGoalRepository
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
class ReadingGoalViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingGoalRepository: ReadingGoalRepository
) : ViewModel() {

    private val _stateUi: MutableStateFlow<ReadingGoalUiState> =
        MutableStateFlow(ReadingGoalUiState())
    val stateUi: StateFlow<ReadingGoalUiState>
        get() = _stateUi.asStateFlow()

    init {
        viewModelScope.launch {
            val currentYear: Int = Date().year + 1900
            readingGoalRepository.getByYear(currentYear).collectLatest { readingGoal ->
                if (readingGoal != null) {
                    _stateUi.update { it.copy(readingGoals = readingGoal.goal) }
                } else {
                    readingGoalRepository.insert(ReadingGoal(year = currentYear))
                }
            }
        }
        viewModelScope.launch {
            bookRepository.getAll().collectLatest { books ->
                val currentYearFinishedBooks: List<Book> = books.filter {
                    it.isFinished && (it.finishedDate != null) && (it.finishedDate.year == Date().year)
                }.sortedByDescending { it.finishedDate }
                _stateUi.update { state ->
                    state.copy(
                        books = currentYearFinishedBooks,
                        readBooksCount = currentYearFinishedBooks.size,
                        readPages = currentYearFinishedBooks
                            .map { it.pageAmount }
                            .reduce { sum, pages -> sum + pages }
                    )
                }
            }
        }
    }
}

data class ReadingGoalUiState(
    val books: List<Book> = emptyList(),
    val readBooksCount: Int = 0,
    val readingGoals: Int = 0,
    val readPages: Int = 0
)