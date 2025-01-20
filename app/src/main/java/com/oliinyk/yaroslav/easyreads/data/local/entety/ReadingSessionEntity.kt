package com.oliinyk.yaroslav.easyreads.data.local.entety

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSessionRecordStatusType
import java.util.Date
import java.util.UUID


@Entity("reading_sessions")
data class ReadingSessionEntity(
    @PrimaryKey
    val id: UUID,
    @ColumnInfo("book_id")
    val bookId: UUID?,

    @ColumnInfo("started_date")
    val startedDate: Date,
    @ColumnInfo("updated_date")
    val updatedDate: Date,

    @ColumnInfo("read_time_in_milliseconds")
    val readTimeInMilliseconds: Long,
    @ColumnInfo("start_page")
    val startPage: Int,
    @ColumnInfo("end_page")
    val endPage: Int,
    @ColumnInfo("read_pages")
    val readPages: Int,

    @ColumnInfo("record_status")
    val recordStatus: String
)

fun ReadingSessionEntity.toModel(): ReadingSession = ReadingSession(
    id = id,
    bookId = bookId,
    startedDate = startedDate,
    updatedDate = updatedDate,
    readTimeInMilliseconds = readTimeInMilliseconds,
    startPage = startPage,
    endPage = endPage,
    readPages = readPages,
    recordStatus = ReadingSessionRecordStatusType.valueOf(recordStatus)
)