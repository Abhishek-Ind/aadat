package com.aadat.app.domain.model

import java.time.LocalDate

data class Habit(
    val id: String,
    val name: String,
    val colorHex: String,
    val frequencyType: FrequencyType,
    val timesPerPeriod: Int,
    val everyNDays: Int,
    val notificationTimeHour: Int?,
    val notificationTimeMinute: Int?,
    val createdAt: Long,
    val isArchived: Boolean
)

enum class FrequencyType { DAILY, WEEKLY, MONTHLY, CUSTOM }

data class HabitCompletion(
    val id: String,
    val habitId: String,
    val completedOn: String
)

data class User(
    val id: String,
    val email: String,
    val isEmailConfirmed: Boolean
)
