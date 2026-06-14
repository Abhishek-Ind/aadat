package com.aadat.app.domain.model

enum class GrowthLevel {
    BARE_SOIL, SEEDLING, SPROUT, SMALL_PLANT,
    FLOWERING_1, FLOWERING_2, FULL_BLOOM
}

enum class HealthState { HEALTHY, WILTING, DEAD }

enum class GardenEvent { BLOOM, WILT, RECOVER }

data class GardenState(
    val growthLevel: GrowthLevel,
    val healthState: HealthState,
    val flowerCount: Int,
    val daysToNextFlower: Int,
    val displayEmoji: String,
    val animationTrigger: GardenEvent?
)
