package com.example.dishcovery.features.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NutritionSummary(
    val totalCalories: Int = 0,
    val targetCalories: Int = 2000,
    val fatsGrams: Int = 0,
    val fatsPercent: Int = 0,
    val proteinGrams: Int = 0,
    val proteinPercent: Int = 0,
    val carbsGrams: Int = 0,
    val carbsPercent: Int = 0
)

data class DashboardUiState(
    val todaysMeals: List<Recipe> = emptyList(),
    val nutritionSummary: NutritionSummary = NutritionSummary(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // TODO: Replace with actual repository call
            val meals = getTodaysMeals()
            val nutrition = calculateNutrition(meals)

            _uiState.value = _uiState.value.copy(
                todaysMeals = meals,
                nutritionSummary = nutrition,
                isLoading = false
            )
        }
    }

    private fun getTodaysMeals(): List<Recipe> {
        // TODO: Replace with actual data
        return listOf(
            Recipe(
                id = "id1",
                name = "Toast with egg",
                imageRes = com.example.dishcovery.R.drawable.ic_launcher_background,
                prepTime = 10,
                calories = 400,
                category = "Breakfast"
            ),
            Recipe(
                id = "id2",
                name = "French Grated Carrot Salad",
                imageRes = com.example.dishcovery.R.drawable.ic_launcher_background,
                prepTime = 15,
                calories = 500,
                category = "Lunch"
            ),
            Recipe(
                id = "id3",
                name = "Salmon Steak",
                imageRes = com.example.dishcovery.R.drawable.ic_launcher_background,
                prepTime = 25,
                calories = 700,
                category = "Dinner"
            )
        )
    }

    private fun calculateNutrition(meals: List<Recipe>): NutritionSummary {
        val totalCalories = meals.sumOf { it.calories }
        val targetCalories = 2000

        return NutritionSummary(
            totalCalories = totalCalories,
            targetCalories = targetCalories,
            fatsGrams = 45,
            fatsPercent = 25,
            proteinGrams = 75,
            proteinPercent = 45,
            carbsGrams = 45,
            carbsPercent = 25
        )
    }

}