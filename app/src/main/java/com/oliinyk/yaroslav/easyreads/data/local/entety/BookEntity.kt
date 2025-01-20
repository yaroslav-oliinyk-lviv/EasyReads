package com.oliinyk.yaroslav.easyreads.data.local.entety

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType
import java.util.Date
import java.util.UUID

@Entity("books")
data class BookEntity(
    @PrimaryKey
    val id: UUID,

    val title: String,
    val author: String,
    val description: String,

    @ColumnInfo(defaultValue = "WANT_TO_READ")
    val shelve: String,

    @ColumnInfo("page_amount")
    val pageAmount: Int,
    @ColumnInfo("page_current")
    val pageCurrent: Int,

    @ColumnInfo("added_date")
    val addedDate: Date,
    @ColumnInfo("updated_date")
    val updatedDate: Date,

    @ColumnInfo("cover_image_file_name")
    val coverImageFileName: String? = null
)

fun BookEntity.toModel(): Book = Book (
    id = id,
    title = title,
    author = author,
    description = description,
    shelve = BookShelveType.valueOf(shelve),
    pageAmount = pageAmount,
    pageCurrent = pageCurrent,
    addedDate = addedDate,
    updatedDate = updatedDate,
    coverImageFileName = coverImageFileName
)