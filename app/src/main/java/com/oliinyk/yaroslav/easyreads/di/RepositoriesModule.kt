package com.oliinyk.yaroslav.easyreads.di

import com.oliinyk.yaroslav.easyreads.data.repository.BookRepositoryImpl
import com.oliinyk.yaroslav.easyreads.data.repository.NoteRepositoryImpl
import com.oliinyk.yaroslav.easyreads.data.repository.PreferencesRepositoryImpl
import com.oliinyk.yaroslav.easyreads.data.repository.ReadingSessionRepositoryImpl
import com.oliinyk.yaroslav.easyreads.data.repository.ReadTimeCounterRepositoryImpl
import com.oliinyk.yaroslav.easyreads.domain.repository.BookRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.NoteRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.PreferencesRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingSessionRepository
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadTimeCounterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {

    @Binds
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    abstract fun bindReadingSessionRepository(impl: ReadingSessionRepositoryImpl): ReadingSessionRepository

    @Binds
    abstract fun bindReadTimeCounterRepository(impl: ReadTimeCounterRepositoryImpl): ReadTimeCounterRepository
}