package com.aadat.app.data.local.dao

import androidx.room.*
import com.aadat.app.data.local.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completedOn = :date")
    suspend fun deleteCompletion(habitId: String, date: String)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedOn DESC")
    fun getCompletionsForHabit(habitId: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedOn >= :from AND completedOn <= :to ORDER BY completedOn DESC")
    fun getCompletionsForHabitInRange(habitId: String, from: String, to: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT COUNT(*) > 0 FROM habit_completions WHERE habitId = :habitId AND completedOn = :date")
    suspend fun isCompletedOnDate(habitId: String, date: String): Boolean
}
