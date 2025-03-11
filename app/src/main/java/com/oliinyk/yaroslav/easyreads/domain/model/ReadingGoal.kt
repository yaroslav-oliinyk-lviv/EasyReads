package com.oliinyk.yaroslav.easyreads.domain.model

import android.os.Parcelable
import com.oliinyk.yaroslav.easyreads.data.local.entety.ReadingGoalEntity
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ReadingGoal(
    val year: Int = Date().year + 1900,

    val goal: Int = 0
): Parcelable

fun ReadingGoal.toEntity(): ReadingGoalEntity = ReadingGoalEntity(
    year = year,
    goal = goal
)