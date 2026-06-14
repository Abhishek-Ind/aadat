package com.aadat.app.domain.usecase

import com.aadat.app.domain.model.GardenEvent
import com.aadat.app.domain.model.GardenState
import com.aadat.app.domain.model.GrowthLevel
import com.aadat.app.domain.model.HealthState
import com.aadat.app.domain.model.StreakResult
import javax.inject.Inject

class GardenStateCalculator @Inject constructor() {

    fun calculate(
        totalCompletions: Int,
        streakResult: StreakResult,
        previousTotalCompletions: Int = 0
    ): GardenState {
        val growthLevel = when {
            totalCompletions == 0 -> GrowthLevel.BARE_SOIL
            totalCompletions in 1..3 -> GrowthLevel.SEEDLING
            totalCompletions in 4..6 -> GrowthLevel.SPROUT
            totalCompletions in 7..13 -> GrowthLevel.SMALL_PLANT
            totalCompletions in 14..20 -> GrowthLevel.FLOWERING_1
            totalCompletions in 21..27 -> GrowthLevel.FLOWERING_2
            else -> GrowthLevel.FULL_BLOOM
        }

        val healthState = when {
            streakResult.currentStreak >= 1 || streakResult.isCompletedToday -> HealthState.HEALTHY
            streakResult.currentStreak == 0 && totalCompletions > 0 -> HealthState.WILTING
            else -> HealthState.DEAD
        }

        val flowerCount = when {
            totalCompletions < 14 -> 0
            else -> (totalCompletions - 14) / 7 + 1
        }

        val nextFlowerThreshold = if (totalCompletions < 14) 14 else {
            val flowersEarned = (totalCompletions - 14) / 7 + 1
            14 + flowersEarned * 7
        }
        val daysToNextFlower = maxOf(0, nextFlowerThreshold - totalCompletions)

        val animationTrigger = when {
            previousTotalCompletions > 0 && totalCompletions > previousTotalCompletions -> {
                val prevFlowers = if (previousTotalCompletions < 14) 0 else (previousTotalCompletions - 14) / 7 + 1
                val currFlowers = flowerCount
                if (currFlowers > prevFlowers) GardenEvent.BLOOM else null
            }
            streakResult.currentStreak == 0 && previousTotalCompletions > 0 -> GardenEvent.WILT
            else -> null
        }

        val displayEmoji = when (growthLevel) {
            GrowthLevel.BARE_SOIL -> "🟫"
            GrowthLevel.SEEDLING -> "🌱"
            GrowthLevel.SPROUT -> "🪴"
            GrowthLevel.SMALL_PLANT -> "🌿"
            GrowthLevel.FLOWERING_1 -> "🌸"
            GrowthLevel.FLOWERING_2 -> "🌺"
            GrowthLevel.FULL_BLOOM -> "🌻"
        }.let { base ->
            if (healthState == HealthState.WILTING || healthState == HealthState.DEAD) "🥀" else base
        }

        return GardenState(
            growthLevel = growthLevel,
            healthState = healthState,
            flowerCount = flowerCount,
            daysToNextFlower = daysToNextFlower,
            displayEmoji = displayEmoji,
            animationTrigger = animationTrigger
        )
    }
}
