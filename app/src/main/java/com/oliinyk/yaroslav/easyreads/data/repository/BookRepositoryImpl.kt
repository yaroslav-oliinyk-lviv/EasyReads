package com.oliinyk.yaroslav.easyreads.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.oliinyk.yaroslav.easyreads.data.local.dao.BookDao
import com.oliinyk.yaroslav.easyreads.data.local.entety.toModel
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookSorting
import com.oliinyk.yaroslav.easyreads.domain.model.BookSortingType
import com.oliinyk.yaroslav.easyreads.domain.model.toEntity
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
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

private const val QUERY_SELECT_ALL_ORDER_BY = "SELECT * FROM books ORDER BY"

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {

    private val coroutineScope: CoroutineScope = GlobalScope
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getAllSortedBy(bookSorting: BookSorting): Flow<List<Book>> {
        val columns = when (bookSorting.bookSortingType) {
            BookSortingType.AUTHOR -> {
                "${BookSortingType.AUTHOR.toString().lowercase()
                } ${bookSorting.bookSortingOrderType
                }, ${BookSortingType.TITLE.toString().lowercase()} ASC"
            }
            else -> {
                "${
                    bookSorting.bookSortingType.toString().lowercase()
                } ${
                    bookSorting.bookSortingOrderType
                }"
            }
        }
        val query = "$QUERY_SELECT_ALL_ORDER_BY $columns"
        return bookDao.getAllSortedBy(SimpleSQLiteQuery(query)).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getById(id: UUID): Flow<Book?> {
        return bookDao.getById(id).map { entity -> entity?.toModel() }
            .distinctUntilChanged()
    }

    override fun save(book: Book) {
        coroutineScope.launch(coroutineDispatcher) {
            bookDao.save(book.toEntity())
        }
    }

    override fun update(book: Book) {
        coroutineScope.launch(coroutineDispatcher) {
            bookDao.update(book.toEntity())
        }
    }

    override fun remove(book: Book) {
        coroutineScope.launch(coroutineDispatcher) {
            bookDao.remove(book.toEntity())
        }
    }
}