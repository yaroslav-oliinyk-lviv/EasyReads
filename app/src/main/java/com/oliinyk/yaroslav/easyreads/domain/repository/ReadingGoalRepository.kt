package com.oliinyk.yaroslav.easyreads.domain.repository

import com.oliinyk.yaroslav.easyreads.domain.model.ReadingGoal
import kotlinx.coroutines.flow.Flow

interface ReadingGoalRepository {

    fun getByYear(year: Int): Flow<ReadingGoal?>

    fun insert(readingGoal: ReadingGoal)

    fun update(readingGoal: ReadingGoal)
}