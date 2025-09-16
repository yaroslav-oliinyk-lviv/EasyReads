package com.oliinyk.yaroslav.easyreads.presentation.reading_goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingGoal
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingGoalRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MILLISECONDS_IN_ONE_MINUTE
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MINUTES_IN_ONE_HOUR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt

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
                if (books.isNotEmpty()) {
                    val currentYearFinishedBooks: List<Book> = books.filter {
                            it.isFinished && (it.finishedDate != null) && (it.finishedDate.year == Date().year)
                        }.sortedByDescending { it.finishedDate }
                    val readPages = if (currentYearFinishedBooks.isNotEmpty()) {
                            currentYearFinishedBooks.map { it.pageAmount }
                                .reduce { sum, pages -> sum + pages }
                        } else { 0 }

                    _stateUi.update { state ->
                        state.copy(
                            books = currentYearFinishedBooks,
                            readBooksCount = currentYearFinishedBooks.size,
                            readPages = readPages
                        )
                    }

                    val sessions = readingSessionRepository.getAllByBookIds(
                        currentYearFinishedBooks.map { it.id }.toList()
                    )
                    if (sessions.isNotEmpty()) {
                        val totalReadTimeInMilliseconds = sessions.map { it.readTimeInMilliseconds }
                            .reduce { acc, value -> acc + value }
                        val totalReadMinutes = totalReadTimeInMilliseconds / MILLISECONDS_IN_ONE_MINUTE

                        _stateUi.update { state ->
                            state.copy(
                                averagePagesHour = (
                                        state.readPages.toDouble() / totalReadMinutes * MINUTES_IN_ONE_HOUR
                                    ).roundToInt(),
                                readHours = (totalReadMinutes / MINUTES_IN_ONE_HOUR).toInt(),
                                readMinutes = (totalReadMinutes % MINUTES_IN_ONE_HOUR).toInt()
                            )
                        }
                    }
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
    val averagePagesHour: Int = 0,
    val readHours: Int = 0,
    val readMinutes: Int = 0
)