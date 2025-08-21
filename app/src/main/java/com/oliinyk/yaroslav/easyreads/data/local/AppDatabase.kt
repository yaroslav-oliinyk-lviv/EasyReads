package com.oliinyk.yaroslav.easyreads.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oliinyk.yaroslav.easyreads.data.local.converter.DateTypeConverter
import com.oliinyk.yaroslav.easyreads.data.local.dao.BookDao
import com.oliinyk.yaroslav.easyreads.data.local.dao.NoteDao
import com.oliinyk.yaroslav.easyreads.data.local.dao.ReadingGoalDao
import com.oliinyk.yaroslav.easyreads.data.local.dao.ReadingSessionDao
import com.oliinyk.yaroslav.easyreads.data.local.entety.BookEntity
import com.oliinyk.yaroslav.easyreads.data.local.entety.NoteEntity
import com.oliinyk.yaroslav.easyreads.data.local.entety.ReadingGoalEntity
import com.oliinyk.yaroslav.easyreads.data.local.entety.ReadingSessionEntity

@Database(
    entities = [
        BookEntity::class,
        NoteEntity::class,
        ReadingSessionEntity::class,
        ReadingGoalEntity::class
    ],
    version = 12
)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun noteDao(): NoteDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun readingGoalDao(): ReadingGoalDao

    companion object {
        const val DATABASE_NAME = "AppDatabase.db"
    }
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `books` ADD COLUMN cover_image_file_name TEXT"
        )
    }
}

val migration_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notes` (`id` BLOB NOT NULL, `book_id` BLOB, `text` TEXT NOT NULL, `page` INTEGER, `added_date` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}

val migration_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `books` ADD COLUMN shelve TEXT NOT NULL DEFAULT 'WANT_TO_READ'"
        )
    }
}

val migration_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `reading_sessions` (`id` BLOB NOT NULL, `book_id` BLOB, `started_date` INTEGER NOT NULL, `read_time_in_seconds` INTEGER NOT NULL, `start_page` INTEGER NOT NULL, `end_page` INTEGER NOT NULL, `read_pages` INTEGER NOT NULL, `record_status` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}

val migration_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `reading_sessions` ADD COLUMN `updated_date` INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val migration_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `reading_sessions` RENAME COLUMN `read_time_in_seconds` to `read_time_in_milliseconds`"
        )
    }
}

val migration_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `books` ADD COLUMN `updated_date` INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val migration_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `books` ADD COLUMN `finished_date` INTEGER")
        db.execSQL("ALTER TABLE `books` ADD COLUMN `is_finished` TEXT NOT NULL DEFAULT FALSE")
    }
}

val migration_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `reading_goals` (`year` INTEGER NOT NULL, `goal` INTEGER NOT NULL, PRIMARY KEY(`year`))"
        )
    }
}

val migration_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `books` ADD COLUMN `read_minutes_count` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `books` ADD COLUMN `read_sessions_count` INTEGER NOT NULL DEFAULT 0")
    }
}

val migration_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `books` ADD COLUMN `isbn` TEXT NOT NULL DEFAULT ''")
    }
}