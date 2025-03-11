package com.oliinyk.yaroslav.easyreads.data.repository

import com.oliinyk.yaroslav.easyreads.data.local.dao.ReadingGoalDao
import com.oliinyk.yaroslav.easyreads.data.local.entety.toModel
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingGoal
import com.oliinyk.yaroslav.easyreads.domain.model.toEntity
import com.oliinyk.yaroslav.easyreads.domain.repository.ReadingGoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingGoalRepositoryImpl @Inject constructor(
    private val readingGoalDao: ReadingGoalDao
) : ReadingGoalRepository {

    private val coroutineScope: CoroutineScope = GlobalScope
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getByYear(year: Int): Flow<ReadingGoal?> {
        return readingGoalDao.getByYear(year)
            .map { it?.toModel() }
            .distinctUntilChanged()
    }

    override fun insert(readingGoal: ReadingGoal) {
        coroutineScope.launch(coroutineDispatcher) {
            readingGoalDao.insert(readingGoal.toEntity())
        }
    }

    override fun update(readingGoal: ReadingGoal) {
        coroutineScope.launch(coroutineDispatcher) {
            readingGoalDao.update(readingGoal.toEntity())
        }
    }
}