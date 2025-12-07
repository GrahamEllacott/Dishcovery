package com.example.dishcovery.features.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.MealPlanDBHelper
import com.example.dishcovery.data.models.MealPlan
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class NutritionSummary(
    val totalCalories: Int = 0,
    val targetCalories: Int = 2000,
    val caloriesPercent: Float = 0f,

    val fatsGrams: Int = 0,
    val targetFats: Int = 75,
    val fatsPercent: Float = 0f,

    val proteinGrams: Int = 0,
    val targetProtein: Int = 60,
    val proteinPercent: Float = 0f,

    val carbsGrams: Int = 0,
    val targetCarbs: Int = 325,
    val carbsPercent: Float = 0f
)

data class DashboardUiState(
    val todaysMeals: List<Recipe> = emptyList(),
    val nutritionSummary: NutritionSummary = NutritionSummary(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecipeRepository = RecipeRepository(application)
    private val mealPlanDBHelper: MealPlanDBHelper = MealPlanDBHelper(application)
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

    private suspend fun getTodaysMeals(): List<Recipe> {
        val date = LocalDate.now()
        var mealPlan = mealPlanDBHelper.getMealPlanByDate(date.toString())

        // If no meal plan exists, create one
        if (mealPlan == null) {
            mealPlan = MealPlan(
                date = date,
                recipeIds = emptyList(),
                recipes = emptyList()
            )
            Log.d("WeeklyMealPlanViewModel", "Creating new meal plan for $date")
            mealPlanDBHelper.insertMealPlan(mealPlan)
        }

        // If there are recipe IDs, fetch the actual recipes
        if (mealPlan.recipeIds.isNotEmpty()) {
            val recipes = mealPlan.recipeIds.mapNotNull { recipeId ->
                // Get the recipe; mapNotNull will discard any null results
                repository.getRecipeById(recipeId).getOrNull()
            }
        }

        return mealPlan.recipes
    }

    private fun calculateNutrition(meals: List<Recipe>): NutritionSummary {
        val totalCalories = meals.sumOf { it.calories }
        val targetCalories = 2000
        val caloriesPercent = (totalCalories.toFloat() / targetCalories.toFloat())

        val fatsGrams = meals.sumOf { it.fat }
        val targetFats = 75
        val fatsPercent = (fatsGrams.toFloat() / targetFats.toFloat())

        val proteinGrams = meals.sumOf { it.protein }
        val targetProtein = 60
        val proteinPercent = (proteinGrams.toFloat() / targetProtein.toFloat())

        val carbsGrams = meals.sumOf { it.carbs }
        val targetCarbs = 325
        val carbsPercent = (carbsGrams.toFloat() / targetCarbs.toFloat())

        return NutritionSummary(
            totalCalories = totalCalories,
            targetCalories = targetCalories,
            caloriesPercent = caloriesPercent,
            fatsGrams = fatsGrams,
            targetFats = targetFats,
            fatsPercent = fatsPercent,
            proteinGrams = proteinGrams,
            targetProtein = targetProtein,
            proteinPercent = proteinPercent,
            carbsGrams = carbsGrams,
            targetCarbs = targetCarbs,
            carbsPercent = carbsPercent
        )
    }

}