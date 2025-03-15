package com.oliinyk.yaroslav.easyreads.presentation.book.edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.util.deleteBookCoverImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BookEditViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _stateUi: MutableStateFlow<StateUiBookEdit> = MutableStateFlow(
        StateUiBookEdit()
    )
    val stateUi: StateFlow<StateUiBookEdit> = _stateUi.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.getAll().collectLatest { books ->
                _stateUi.update { it.copy(
                    authors = books.map { book -> book.author }
                        .distinct()
                ) }
            }
        }
    }

    fun updateStateUi(onUpdate: (StateUiBookEdit) -> StateUiBookEdit) {
        _stateUi.update { onUpdate(it) }
    }

    fun save(contextApplication: Context) {
        _stateUi.value.book?.let {
            var saveBook = it
            if (_stateUi.value.isNewImageCopied) {
                deleteBookCoverImage(contextApplication, saveBook.coverImageFileName)
                saveBook = saveBook.copy(coverImageFileName = _stateUi.value.pickedImageName)
            } else {
                deleteBookCoverImage(contextApplication, _stateUi.value.pickedImageName)
            }
            if (!saveBook.isFinished && saveBook.shelve == BookShelveType.FINISHED) {
                saveBook = saveBook.copy(isFinished = true, finishedDate = Date())
            } else if (saveBook.isFinished && saveBook.shelve != BookShelveType.FINISHED) {
                saveBook = saveBook.copy(isFinished = false, finishedDate = null)
            }

            bookRepository.save(saveBook)
        }
    }

    suspend fun updateCoverImage(applicationContext: Context, uri: Uri) {
        _stateUi.value.pickedImageName?.let {
            deleteBookCoverImage(applicationContext, it)
        }
        _stateUi.update {
            it.copy(
                pickedImageUri = uri,
                pickedImageName = "IMG_${UUID.randomUUID()}.JPG"
            )
        }
        copyImageToAppFolder(applicationContext)
    }

    private suspend fun copyImageToAppFolder(contextApplication: Context) {
        val pickedImageUri = _stateUi.value.pickedImageUri ?: return
        val pickedImageName = _stateUi.value.pickedImageName ?: return

        val destinationFile = File(contextApplication.filesDir, pickedImageName)
        try {
            contextApplication.contentResolver.openInputStream(pickedImageUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(1024 * 8)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }

            _stateUi.update { it.copy(isNewImageCopied = true) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun removeUnusedCoverImage(applicationContext: Context) {
        if (_stateUi.value.isNewImageCopied) {
            deleteBookCoverImage(
                applicationContext,
                _stateUi.value.pickedImageName
            )
            _stateUi.update { it.copy(isNewImageCopied = false) }
        }
    }
}

data class StateUiBookEdit(
    val book: Book? = null,
    val pickedImageUri: Uri? = null,
    val pickedImageName: String? = null,
    val tookPhotoName: String? = null,
    val isNewImageCopied: Boolean = false,
    val authors: List<String> = emptyList()
)