package com.oliinyk.yaroslav.easyreads.di

import android.content.Context
import androidx.room.Room
import com.oliinyk.yaroslav.easyreads.data.local.AppDatabase
import com.oliinyk.yaroslav.easyreads.data.local.dao.BookDao
import com.oliinyk.yaroslav.easyreads.data.local.dao.NoteDao
import com.oliinyk.yaroslav.easyreads.data.local.dao.ReadingSessionDao
import com.oliinyk.yaroslav.easyreads.data.local.migration_1_2
import com.oliinyk.yaroslav.easyreads.data.local.migration_2_3
import com.oliinyk.yaroslav.easyreads.data.local.migration_3_4
import com.oliinyk.yaroslav.easyreads.data.local.migration_4_5
import com.oliinyk.yaroslav.easyreads.data.local.migration_5_6
import com.oliinyk.yaroslav.easyreads.data.local.migration_6_7
import com.oliinyk.yaroslav.easyreads.data.local.migration_7_8
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideReadingSessionDao(database: AppDatabase): ReadingSessionDao {
        return database.readingSessionDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
            .addMigrations(
                migration_1_2,
                migration_2_3,
                migration_3_4,
                migration_4_5,
                migration_5_6,
                migration_6_7,
                migration_7_8
            )
            .build()
    }
}