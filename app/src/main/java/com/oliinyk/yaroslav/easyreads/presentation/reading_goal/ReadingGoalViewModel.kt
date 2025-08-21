package com.oliinyk.yaroslav.easyreads.presentation.reading_goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingGoal
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingGoalRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MILLISECONDS_IN_ONE_SECOND
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.SECONDS_IN_ONE_MINUTE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReadingGoalViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingGoalRepository: ReadingGoalRepository,
    private val readingSessionRepository: ReadingSessionRepository
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
            bookRepository.getAll().collect { books ->
                _stateUi.update { it.copy(totalReadMinutes = 0) }

                val currentYearFinishedBooks: List<Book> = books
                    .filter {
                        it.isFinished && (it.finishedDate != null) && (it.finishedDate.year == Date().year)
                    }
                    .sortedByDescending { it.finishedDate }

                currentYearFinishedBooks.forEach { book ->
                    viewModelScope.launch {
                        var totalReadTimeInSeconds = 0
                        readingSessionRepository
                            .getAllByBookId(book.id)
                            .take(1)
                            .collect { sessions ->
                                if (sessions.isNotEmpty()) {
                                    totalReadTimeInSeconds += sessions
                                        .map { (it.readTimeInMilliseconds / MILLISECONDS_IN_ONE_SECOND).toInt() }
                                        .reduce { acc, value -> acc + value }
                                }
                            }
                        _stateUi.update { state ->
                            state.copy(
                                totalReadMinutes = state.totalReadMinutes + totalReadTimeInSeconds / SECONDS_IN_ONE_MINUTE
                            )
                        }
                    }
                }

                _stateUi.update { state ->
                    state.copy(
                        books = currentYearFinishedBooks,
                        readBooksCount = currentYearFinishedBooks.size,
                        readPages = if (currentYearFinishedBooks.isNotEmpty()) {
                            currentYearFinishedBooks.map { it.pageAmount }
                                .reduce { sum, pages -> sum + pages }
                        } else {
                            0
                        }
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
    val readPages: Int = 0,
    val totalReadMinutes: Int = 0
)