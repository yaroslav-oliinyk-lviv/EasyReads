package com.oliinyk.yaroslav.easyreads.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.oliinyk.yaroslav.easyreads.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("settings")
    }

    override fun getBookListCellHolderSize(): Flow<String> {
        return dataStore.data.map {
            it[PREF_BOOK_LIST_CELL_HOLDER_SIZE] ?: ""
        }.distinctUntilChanged()
    }

    override suspend fun setBookListCellHolderSize(holderSize: String) {
        dataStore.edit {
            it[PREF_BOOK_LIST_CELL_HOLDER_SIZE] = holderSize
        }
    }

    override fun getBookSorting(): Flow<String> {
        return dataStore.data.map {
            it[PREF_BOOK_SORTING] ?: ""
        }.distinctUntilChanged()
    }

    override suspend fun setBookSorting(bookSorting: String) {
        dataStore.edit {
            it[PREF_BOOK_SORTING] = bookSorting
        }
    }

    companion object {
        private val PREF_BOOK_LIST_CELL_HOLDER_SIZE = stringPreferencesKey(
            "PREF_BOOK_LIST_CELL_HOLDER_SIZE"
        )
        private val PREF_BOOK_SORTING = stringPreferencesKey(
            "PREF_BOOK_SORTING"
        )
    }
}