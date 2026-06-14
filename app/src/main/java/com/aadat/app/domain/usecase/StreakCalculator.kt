package com.aadat.app.domain.usecase

import com.aadat.app.domain.model.FrequencyType
import com.aadat.app.domain.model.Habit
import com.aadat.app.domain.model.StreakResult
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class StreakCalculator @Inject constructor() {

    fun calculate(habit: Habit, completionDates: List<LocalDate>): StreakResult {
        val today = LocalDate.now()
        val sortedDates = completionDates.sorted()

        return when (habit.frequencyType) {
            FrequencyType.DAILY -> calculateDaily(habit, sortedDates, today)
            FrequencyType.WEEKLY -> calculatePeriodic(habit, sortedDates, today, PeriodType.WEEK)
            FrequencyType.MONTHLY -> calculatePeriodic(habit, sortedDates, today, PeriodType.MONTH)
            FrequencyType.CUSTOM -> calculateCustom(habit, sortedDates, today)
        }
    }

    private enum class PeriodType { WEEK, MONTH }

    private fun calculateDaily(habit: Habit, sortedDates: List<LocalDate>, today: LocalDate): StreakResult {
        val dateSet = sortedDates.toHashSet()
        val isCompletedToday = today in dateSet

        var current = 0
        var checkDate = today
        while (checkDate in dateSet) {
            current++
            checkDate = checkDate.minusDays(1)
        }

        var longest = 0
        var runLength = 0
        var prev: LocalDate? = null
        for (date in sortedDates) {
            if (prev == null || ChronoUnit.DAYS.between(prev, date) == 1L) {
                runLength++
            } else {
                runLength = 1
            }
            if (runLength > longest) longest = runLength
            prev = date
        }
        if (current > longest) longest = current

        val createdDate = java.time.Instant.ofEpochMilli(habit.createdAt)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val totalPossible = ChronoUnit.DAYS.between(createdDate, today).toInt() + 1

        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekProgress = sortedDates.count { it >= weekStart && it <= today }

        val monthStart = today.withDayOfMonth(1)
        val monthProgress = sortedDates.count { it >= monthStart && it <= today }

        val yearStart = today.withDayOfYear(1)
        val yearProgress = sortedDates.count { it >= yearStart && it <= today }

        return StreakResult(
            currentStreak = current,
            longestStreak = longest,
            isCompletedToday = isCompletedToday,
            allTimeCompletions = sortedDates.size,
            allTimePossible = maxOf(totalPossible, 1),
            weekProgress = weekProgress,
            weekTarget = habit.timesPerPeriod,
            monthProgress = monthProgress,
            monthTarget = habit.timesPerPeriod,
            yearProgress = yearProgress,
            yearTarget = habit.timesPerPeriod
        )
    }

    private fun calculatePeriodic(
        habit: Habit,
        sortedDates: List<LocalDate>,
        today: LocalDate,
        periodType: PeriodType
    ): StreakResult {
        fun periodStart(date: LocalDate): LocalDate = when (periodType) {
            PeriodType.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            PeriodType.MONTH -> date.withDayOfMonth(1)
        }
        fun previousPeriodStart(start: LocalDate): LocalDate = when (periodType) {
            PeriodType.WEEK -> start.minusWeeks(1)
            PeriodType.MONTH -> start.minusMonths(1)
        }

        val dateSet = sortedDates.toHashSet()
        val target = habit.timesPerPeriod

        val currentPeriodStart = periodStart(today)
        val currentPeriodCompletions = dateSet.count { it >= currentPeriodStart && it <= today }
        val isCurrentPeriodHit = currentPeriodCompletions >= target

        var current = 0
        var ps = currentPeriodStart
        while (true) {
            val pe = when (periodType) {
                PeriodType.WEEK -> ps.plusWeeks(1).minusDays(1)
                PeriodType.MONTH -> ps.plusMonths(1).minusDays(1)
            }
            val count = dateSet.count { it >= ps && it <= minOf(pe, today) }
            if (count >= target) {
                current++
                ps = previousPeriodStart(ps)
            } else {
                break
            }
        }

        var longest = 0
        var run = 0
        var checkPs = periodStart(sortedDates.firstOrNull() ?: today)
        val endPs = currentPeriodStart
        while (checkPs <= endPs) {
            val pe = when (periodType) {
                PeriodType.WEEK -> checkPs.plusWeeks(1).minusDays(1)
                PeriodType.MONTH -> checkPs.plusMonths(1).minusDays(1)
            }
            val count = dateSet.count { it >= checkPs && it <= pe }
            if (count >= target) {
                run++
                if (run > longest) longest = run
            } else {
                run = 0
            }
            checkPs = when (periodType) {
                PeriodType.WEEK -> checkPs.plusWeeks(1)
                PeriodType.MONTH -> checkPs.plusMonths(1)
            }
        }

        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekProgress = dateSet.count { it >= weekStart && it <= today }
        val monthStart = today.withDayOfMonth(1)
        val monthProgress = dateSet.count { it >= monthStart && it <= today }
        val yearStart = today.withDayOfYear(1)
        val yearProgress = dateSet.count { it >= yearStart && it <= today }

        val weeksInYear = 52
        val monthsInYear = 12

        return StreakResult(
            currentStreak = current,
            longestStreak = maxOf(longest, current),
            isCompletedToday = isCurrentPeriodHit,
            allTimeCompletions = sortedDates.size,
            allTimePossible = sortedDates.size + maxOf(0, target - currentPeriodCompletions),
            weekProgress = weekProgress,
            weekTarget = if (periodType == PeriodType.WEEK) target else 7,
            monthProgress = monthProgress,
            monthTarget = if (periodType == PeriodType.MONTH) target else 30,
            yearProgress = yearProgress,
            yearTarget = if (periodType == PeriodType.WEEK) weeksInYear * target else monthsInYear * target
        )
    }

    private fun calculateCustom(habit: Habit, sortedDates: List<LocalDate>, today: LocalDate): StreakResult {
        val createdDate = java.time.Instant.ofEpochMilli(habit.createdAt)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val windowDays = habit.everyNDays.toLong()
        val target = habit.timesPerPeriod
        val dateSet = sortedDates.toHashSet()

        fun windowStart(date: LocalDate): LocalDate {
            val daysSinceCreation = ChronoUnit.DAYS.between(createdDate, date)
            val windowIndex = daysSinceCreation / windowDays
            return createdDate.plusDays(windowIndex * windowDays)
        }

        val currentWindowStart = windowStart(today)
        val currentWindowEnd = currentWindowStart.plusDays(windowDays - 1)
        val currentWindowCount = dateSet.count { it >= currentWindowStart && it <= minOf(currentWindowEnd, today) }
        val isCurrentWindowHit = currentWindowCount >= target

        var current = 0
        var ws = currentWindowStart
        while (ws >= createdDate) {
            val we = ws.plusDays(windowDays - 1)
            val count = dateSet.count { it >= ws && it <= minOf(we, today) }
            if (count >= target) {
                current++
                ws = ws.minusDays(windowDays)
            } else {
                break
            }
        }

        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekProgress = dateSet.count { it >= weekStart && it <= today }
        val monthStart = today.withDayOfMonth(1)
        val monthProgress = dateSet.count { it >= monthStart && it <= today }
        val yearStart = today.withDayOfYear(1)
        val yearProgress = dateSet.count { it >= yearStart && it <= today }

        return StreakResult(
            currentStreak = current,
            longestStreak = current,
            isCompletedToday = isCurrentWindowHit,
            allTimeCompletions = sortedDates.size,
            allTimePossible = sortedDates.size + maxOf(0, target - currentWindowCount),
            weekProgress = weekProgress,
            weekTarget = target,
            monthProgress = monthProgress,
            monthTarget = target,
            yearProgress = yearProgress,
            yearTarget = target
        )
    }
}
