package com.oliinyk.yaroslav.easyreads.domain.repository

import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType
import com.oliinyk.yaroslav.easyreads.domain.model.BookSorting
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface BookRepository {

    fun getAllAndSorted(bookSorting: BookSorting): Flow<List<Book>>

    fun getByShelveAndSorted(bookShelveType: BookShelveType, bookSorting: BookSorting): Flow<List<Book>>

    fun getAll(): Flow<List<Book>>

    fun getById(id: UUID): Flow<Book?>

    fun save(book: Book)

    fun update(book: Book)

    fun remove(book: Book)
}