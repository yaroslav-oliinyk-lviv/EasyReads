package com.oliinyk.yaroslav.easyreads.presentation.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _stateUi: MutableStateFlow<List<Note>> = MutableStateFlow(emptyList())
    val stateUi: StateFlow<List<Note>>
        get() = _stateUi.asStateFlow()

    fun loadNotes(bookId: UUID) {
        viewModelScope.launch {
            noteRepository.getAllByBookId(bookId).collect { notes ->
                _stateUi.value = notes
            }
        }
    }

    fun addNote(note: Note) {
        noteRepository.insert(note)
    }

    fun update(note: Note) {
        noteRepository.update(note)
    }

    fun remove(note: Note) {
        noteRepository.remove(note)
    }
}