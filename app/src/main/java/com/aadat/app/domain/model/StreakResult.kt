package com.aadat.app.domain.model

data class StreakResult(
    val currentStreak: Int,
    val longestStreak: Int,
    val isCompletedToday: Boolean,
    val allTimeCompletions: Int,
    val allTimePossible: Int,
    val weekProgress: Int,
    val weekTarget: Int,
    val monthProgress: Int,
    val monthTarget: Int,
    val yearProgress: Int,
    val yearTarget: Int
)
