package com.oliinyk.yaroslav.easyreads.domain.repository

import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import java.util.UUID

interface ReadTimeCounterRepository {

    fun start(bookId: UUID, pageCurrent: Int)

    fun resume()

    fun pause()

    fun stop()

    fun getReadTimeInMilliseconds(): Long
}