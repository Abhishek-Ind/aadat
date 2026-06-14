package com.aadat.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aadat.app.data.local.dao.HabitCompletionDao
import com.aadat.app.data.local.dao.HabitDao
import com.aadat.app.data.local.entity.HabitCompletionEntity
import com.aadat.app.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AadatDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
}
