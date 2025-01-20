package com.oliinyk.yaroslav.easyreads.presentation.book.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookSorting
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _stateUi: MutableStateFlow<StateUiBookList> = MutableStateFlow(StateUiBookList())
    val stateUi: StateFlow<StateUiBookList>
        get() = _stateUi.asStateFlow()

    val bookSorting: BookSorting
        get() = stateUi.value.bookSorting

    private var jobGetAllSortedBy: Job? = null

    init {
        viewModelScope.launch {
            preferencesRepository.getBookListCellHolderSize().collectLatest { holderSizeString ->
                if (holderSizeString.isNotEmpty()) {
                    _stateUi.update {
                        it.copy(
                            holderSize = BookHolder.HolderSize.valueOf(holderSizeString)
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            preferencesRepository.getBookSorting().collectLatest { bookSortingString ->
                if (bookSortingString.isNotEmpty()) {
                    val bookSorting = BookSorting.fromString(bookSortingString)
                    _stateUi.update { it.copy(bookSorting = bookSorting) }
                    loadBooks()
                } else {
                    loadBooks()
                }
            }
        }
    }

    fun updateStateUi(onUpdate: (StateUiBookList) -> StateUiBookList) {
        _stateUi.update { onUpdate(it) }
    }

    fun updateBookSorting(bookSorting: BookSorting) {
        viewModelScope.launch {
            preferencesRepository.setBookSorting(bookSorting.toString())
        }
    }
    fun updateHolderSize(holderSize: BookHolder.HolderSize) {
        viewModelScope.launch {
            preferencesRepository.setBookListCellHolderSize(holderSize.toString())
        }
    }

    private fun loadBooks() {
        jobGetAllSortedBy?.cancel()
        jobGetAllSortedBy = viewModelScope.launch {
            bookRepository.getAllSortedBy(bookSorting).collect { books ->
                _stateUi.update { it.copy(books = books) }
            }
        }
    }
}

data class StateUiBookList(
    val books: List<Book> = emptyList(),
    val holderSize: BookHolder.HolderSize = BookHolder.HolderSize.DEFAULT,
    val bookSorting: BookSorting = BookSorting()
)