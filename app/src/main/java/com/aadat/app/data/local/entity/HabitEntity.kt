package com.aadat.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val frequencyType: String,
    val timesPerPeriod: Int = 1,
    val everyNDays: Int = 1,
    val notificationTimeHour: Int? = null,
    val notificationTimeMinute: Int? = null,
    val createdAt: Long,
    val isArchived: Boolean = false
)
