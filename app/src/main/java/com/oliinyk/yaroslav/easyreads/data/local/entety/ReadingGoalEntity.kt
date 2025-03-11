package com.oliinyk.yaroslav.easyreads.data.local.entety

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingGoal

@Entity("reading_goals")
data class ReadingGoalEntity(

    @PrimaryKey
    val year: Int,

    val goal: Int
)

fun ReadingGoalEntity.toModel(): ReadingGoal = ReadingGoal(
    year = year,
    goal = goal
)