package com.oliinyk.yaroslav.easyreads.data.repository

import com.oliinyk.yaroslav.easyreads.data.local.dao.NoteDao
import com.oliinyk.yaroslav.easyreads.data.local.entety.toModel
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.model.toEntity
import com.oliinyk.yaroslav.easyreads.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    private val coroutineScope: CoroutineScope = GlobalScope
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getAllByBookId(bookId: UUID): Flow<List<Note>> {
        return noteDao.getAllByBookId(bookId)
            .map { entities ->
                entities.map { it.toModel() }
            }
    }

    override fun getLastAddedByBookId(bookId: UUID): Flow<Note?> {
        return noteDao.getLastAddedByBookId(bookId).map { it?.toModel() }
            .distinctUntilChanged()
    }

    override fun insert(note: Note) {
        coroutineScope.launch(coroutineDispatcher) { noteDao.insert(note.toEntity()) }
    }

    override fun update(note: Note) {
        coroutineScope.launch(coroutineDispatcher) { noteDao.update(note.toEntity()) }
    }

    override fun remove(note: Note) {
        coroutineScope.launch(coroutineDispatcher) { noteDao.delete(note.toEntity()) }
    }

    override fun remove(notes: List<Note>) {
        if (notes.isEmpty()) {
            return
        }
        coroutineScope.launch(coroutineDispatcher) { noteDao.delete(notes.map { it.toEntity() }) }
    }
}