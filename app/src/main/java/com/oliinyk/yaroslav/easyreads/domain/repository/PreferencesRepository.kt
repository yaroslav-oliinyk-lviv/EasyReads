package com.oliinyk.yaroslav.easyreads.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    fun getBookListCellHolderSize(): Flow<String>

    suspend fun setBookListCellHolderSize(holderSize: String)

    fun getBookSorting(): Flow<String>

    suspend fun setBookSorting(bookSorting: String)
}