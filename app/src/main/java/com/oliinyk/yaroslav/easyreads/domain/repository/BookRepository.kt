package com.oliinyk.yaroslav.easyreads.domain.repository

import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookSorting
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface BookRepository {

    fun getAllSortedBy(bookSorting: BookSorting): Flow<List<Book>>

    fun getById(id: UUID): Flow<Book?>

    fun save(book: Book)

    fun update(book: Book)

    fun remove(book: Book)
}