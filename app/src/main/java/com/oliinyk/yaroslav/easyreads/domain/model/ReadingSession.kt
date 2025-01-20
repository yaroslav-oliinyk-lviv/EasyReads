package com.oliinyk.yaroslav.easyreads.domain.model

import android.os.Parcelable
import com.oliinyk.yaroslav.easyreads.data.local.entety.ReadingSessionEntity
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MILLISECONDS_IN_ONE_SECOND
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MINUTES_IN_ONE_HOUR
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.SECONDS_IN_ONE_HOUR
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.SECONDS_IN_ONE_MINUTE
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID
import kotlin.math.roundToInt

@Parcelize
data class ReadingSession(
    override val id: UUID = UUID.randomUUID(),
    val bookId: UUID? = null,

    val startedDate: Date = Date(),
    val updatedDate: Date = Date(),

    val readTimeInMilliseconds: Long = 0,
    val startPage: Int = 0,
    val endPage: Int = 0,
    val readPages: Int = 0,

    val recordStatus: ReadingSessionRecordStatusType = ReadingSessionRecordStatusType.STARTED
) : BaseModel(), Parcelable {
    private val readTotalSeconds: Int
        get() =  (readTimeInMilliseconds / MILLISECONDS_IN_ONE_SECOND).toInt()
    private val readTotalMinutes: Int
        get() =  readTotalSeconds / SECONDS_IN_ONE_MINUTE

    val readHours: Int
        get() = readTotalMinutes / MINUTES_IN_ONE_HOUR
    val readMinutes: Int
        get() = readTotalMinutes % MINUTES_IN_ONE_HOUR
    val readSeconds: Int
        get() = readTotalSeconds % SECONDS_IN_ONE_MINUTE

    val readPagesHour: Int
        get() = if (readTotalSeconds != 0) {
            (readPages.toDouble() / readTotalSeconds.toDouble() * SECONDS_IN_ONE_HOUR).roundToInt()
        } else { 0 }
}

fun ReadingSession.toEntity(): ReadingSessionEntity = ReadingSessionEntity(
    id = id,
    bookId = bookId,
    startedDate = startedDate,
    updatedDate = updatedDate,
    readTimeInMilliseconds = readTimeInMilliseconds,
    endPage = endPage,
    startPage = startPage,
    readPages = readPages,
    recordStatus = recordStatus.toString()
)