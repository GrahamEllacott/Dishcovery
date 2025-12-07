package com.example.dishcovery.data.models

import java.time.LocalDate

data class MealPlan(
    val id: Int = 0,
    val date: LocalDate,
    val recipeIds: List<String>,
    val recipes: List<Recipe>
)
