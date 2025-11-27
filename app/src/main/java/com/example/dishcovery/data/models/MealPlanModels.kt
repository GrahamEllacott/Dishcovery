package com.example.dishcovery.data.models

// Sample data classes - to be replaced with actual data models
data class MealPlanItem(
    val id: Int,
    val mealType: String, // "Breakfast", "Lunch", "Dinner"
    val recipeName: String,
    val time: String,
    val calories: Int,
    val imageRes: Int
)

data class DayMealPlan(
    val dayName: String,
    val date: String,
    val meals: List<MealPlanItem?>
)
