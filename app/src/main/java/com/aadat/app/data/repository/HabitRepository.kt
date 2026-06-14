package com.aadat.app.data.repository

import com.aadat.app.data.local.dao.HabitCompletionDao
import com.aadat.app.data.local.dao.HabitDao
import com.aadat.app.data.local.entity.HabitCompletionEntity
import com.aadat.app.data.local.entity.HabitEntity
import com.aadat.app.domain.model.FrequencyType
import com.aadat.app.domain.model.Habit
import com.aadat.app.domain.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) {
    fun getAllActiveHabits(): Flow<List<Habit>> =
        habitDao.getAllActiveHabits().map { list -> list.map { it.toDomain() } }

    fun getHabitById(id: String): Flow<Habit?> =
        habitDao.getHabitById(id).map { it?.toDomain() }

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit.toEntity())

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit.toEntity())

    suspend fun deleteHabit(habitId: String) = habitDao.deleteHabit(habitId)

    fun getCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsForHabit(habitId).map { list -> list.map { it.toDomain() } }

    suspend fun toggleCompletion(habitId: String, date: LocalDate) {
        val dateStr = date.toString()
        val isCompleted = completionDao.isCompletedOnDate(habitId, dateStr)
        if (isCompleted) {
            completionDao.deleteCompletion(habitId, dateStr)
        } else {
            completionDao.insertCompletion(
                HabitCompletionEntity(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    completedOn = dateStr
                )
            )
        }
    }

    suspend fun isCompletedOnDate(habitId: String, date: LocalDate): Boolean =
        completionDao.isCompletedOnDate(habitId, date.toString())

    private fun HabitEntity.toDomain() = Habit(
        id = id,
        name = name,
        colorHex = colorHex,
        frequencyType = FrequencyType.valueOf(frequencyType),
        timesPerPeriod = timesPerPeriod,
        everyNDays = everyNDays,
        notificationTimeHour = notificationTimeHour,
        notificationTimeMinute = notificationTimeMinute,
        createdAt = createdAt,
        isArchived = isArchived
    )

    private fun Habit.toEntity() = HabitEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        frequencyType = frequencyType.name,
        timesPerPeriod = timesPerPeriod,
        everyNDays = everyNDays,
        notificationTimeHour = notificationTimeHour,
        notificationTimeMinute = notificationTimeMinute,
        createdAt = createdAt,
        isArchived = isArchived
    )

    private fun HabitCompletionEntity.toDomain() = HabitCompletion(
        id = id, habitId = habitId, completedOn = completedOn
    )
}
