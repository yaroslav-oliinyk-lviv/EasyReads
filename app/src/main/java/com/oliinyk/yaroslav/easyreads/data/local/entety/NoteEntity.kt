package com.oliinyk.yaroslav.easyreads.data.local.entety

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import java.util.Date
import java.util.UUID

@Entity("notes")
data class NoteEntity(
    @PrimaryKey
    val id: UUID,
    @ColumnInfo("book_id")
    val bookId: UUID?,

    val text: String,
    val page: Int?,

    @ColumnInfo("added_date")
    val addedDate: Date
)

fun NoteEntity.toModel(): Note = Note(
    id = id,
    bookId = bookId,
    text = text,
    page = page,
    addedDate = addedDate
)