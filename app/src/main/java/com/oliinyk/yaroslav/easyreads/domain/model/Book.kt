package com.oliinyk.yaroslav.easyreads.domain.model

import android.os.Parcelable
import com.oliinyk.yaroslav.easyreads.data.local.entety.BookEntity
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class Book(
    override val id: UUID = UUID.randomUUID(),
    val title: String = "",
    val author: String = "",
    val description: String = "",

    val shelve: BookShelveType = BookShelveType.WANT_TO_READ,

    val pageAmount: Int = 0,
    val pageCurrent: Int = 0,

    val addedDate: Date = Date(),
    val updatedDate: Date = Date(),

    val coverImageFileName: String? = null
) : BaseModel(), Parcelable

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    description = description,
    shelve = shelve.toString(),
    pageAmount = pageAmount,
    pageCurrent = pageCurrent,
    addedDate = addedDate,
    updatedDate = updatedDate,
    coverImageFileName = coverImageFileName
)
