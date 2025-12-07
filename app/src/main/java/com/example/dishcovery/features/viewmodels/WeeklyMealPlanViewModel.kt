package com.example.dishcovery.features.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.R
import com.example.dishcovery.data.MealPlanDBHelper
import com.example.dishcovery.data.models.MealPlan
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeeklyMealPlanUiState(
    val week: LocalDate? = null,
    val mealPlans: List<MealPlan> = emptyList(),
    val allRecipes: List<Recipe> = emptyList(),
    val isLoadingRecipes: Boolean = false,
    val showRecipeDialog: Boolean = false,
    val selectedDate: LocalDate? = null,
    val selectedMealType: String? = null,
    val selectedMealIndex: Int = -1
)

class WeeklyMealPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecipeRepository = RecipeRepository(application)
    private val mealPlanDBHelper: MealPlanDBHelper = MealPlanDBHelper(application)
    private val _uiState = MutableStateFlow(WeeklyMealPlanUiState())
    val uiState: StateFlow<WeeklyMealPlanUiState> = _uiState.asStateFlow()

    init {
        setCurrentWeek()
        getMealPlans()
        loadAllRecipes()
    }

    private fun setCurrentWeek() {
        val currentWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        _uiState.value = _uiState.value.copy(week = currentWeek)
    }

    fun updateWeek(week: LocalDate) {
        _uiState.value = _uiState.value.copy(week = week)
        getMealPlans()
    }

    fun previousWeek() {
        _uiState.value = _uiState.value.copy(week = _uiState.value.week!!.minusWeeks(1))
        getMealPlans()
    }

    fun nextWeek() {
        _uiState.value = _uiState.value.copy(week = _uiState.value.week!!.plusWeeks(1))
        getMealPlans()
    }

    private fun loadAllRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRecipes = true)
            val result = repository.getRecipes()
            _uiState.value = _uiState.value.copy(
                allRecipes = result.getOrNull() ?: emptyList(),
                isLoadingRecipes = false
            )
        }
    }

    fun showRecipeSelectionDialog(date: LocalDate, mealType: String) {
        // Determine the meal index based on meal type
        val context = getApplication<Application>()
        val mealIndex = when (mealType) {
            context.getString(R.string.meal_breakfast) -> 0
            context.getString(R.string.meal_lunch) -> 1
            context.getString(R.string.meal_dinner) -> 2
            else -> -1
        }

        _uiState.value = _uiState.value.copy(
            showRecipeDialog = true,
            selectedDate = date,
            selectedMealType = mealType,
            selectedMealIndex = mealIndex
        )
    }

    fun hideRecipeSelectionDialog() {
        _uiState.value = _uiState.value.copy(
            showRecipeDialog = false,
            selectedDate = null,
            selectedMealType = null,
            selectedMealIndex = -1
        )
    }

    fun addRecipeToMealPlan(recipe: Recipe) {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate ?: return@launch
            val mealIndex = _uiState.value.selectedMealIndex

            if (mealIndex < 0 || mealIndex > 2) return@launch

            val mealPlan = mealPlanDBHelper.getMealPlanByDate(date.toString())

            if (mealPlan != null) {
                // Create a mutable list with exactly 3 slots
                val updatedRecipeIds = mealPlan.recipeIds.toMutableList()

                // Ensure we have 3 slots
                while (updatedRecipeIds.size < 3) {
                    updatedRecipeIds.add("")
                }

                // Set the recipe at the specific meal index
                updatedRecipeIds[mealIndex] = recipe.id

                val updatedMealPlan = mealPlan.copy(recipeIds = updatedRecipeIds)
                mealPlanDBHelper.updateMealPlan(updatedMealPlan)

                Log.d("WeeklyMealPlanViewModel", "Added recipe ${recipe.name} to slot $mealIndex for $date")
            }

            // Refresh meal plans
            getMealPlans()
            hideRecipeSelectionDialog()
        }
    }

    fun removeRecipeFromMealPlan(date: LocalDate, recipeId: String) {
        viewModelScope.launch {
            val mealPlan = mealPlanDBHelper.getMealPlanByDate(date.toString())

            if (mealPlan != null) {
                val updatedRecipeIds = mealPlan.recipeIds.toMutableList()

                // Ensure we have 3 slots
                while (updatedRecipeIds.size < 3) {
                    updatedRecipeIds.add("")
                }

                // Find the index of the recipe and replace it with empty string
                val index = updatedRecipeIds.indexOf(recipeId)
                if (index != -1) {
                    updatedRecipeIds[index] = ""
                    Log.d("WeeklyMealPlanViewModel", "Removed recipe from slot $index for $date")
                }

                val updatedMealPlan = mealPlan.copy(recipeIds = updatedRecipeIds)
                mealPlanDBHelper.updateMealPlan(updatedMealPlan)
            }

            // Refresh meal plans
            getMealPlans()
        }
    }

    private fun getMealPlans() {
        viewModelScope.launch {
            val mealPlansForWeek = (0..6).map { i ->
                val date = _uiState.value.week!!.plusDays(i.toLong())
                var mealPlan = mealPlanDBHelper.getMealPlanByDate(date.toString())

                if (mealPlan == null) {
                    // Create with 3 empty slots
                    mealPlan = MealPlan(
                        date = date,
                        recipeIds = listOf("", "", ""),
                        recipes = emptyList()
                    )
                    Log.d("WeeklyMealPlanViewModel", "Creating new meal plan for $date")
                    mealPlanDBHelper.insertMealPlan(mealPlan)
                }

                // Ensure we have exactly 3 slots
                val recipeIds = mealPlan.recipeIds.toMutableList()
                while (recipeIds.size < 3) {
                    recipeIds.add("")
                }

                // CRITICAL FIX: Use map instead of mapNotNull to preserve null slots
                // This maintains the 3-slot structure: [Recipe, null, Recipe] stays as is
                val recipes = recipeIds.map { recipeId ->
                    if (recipeId.isNotEmpty()) {
                        repository.getRecipeById(recipeId).getOrNull()
                    } else {
                        null
                    }
                }

                Log.d("WeeklyMealPlanViewModel", "Meal plan for $date: ${recipeIds.size} slots, ${recipes.filterNotNull().size} recipes")

                mealPlan.copy(recipeIds = recipeIds, recipes = recipes.filterNotNull())
            }
            _uiState.value = _uiState.value.copy(mealPlans = mealPlansForWeek)
        }
    }
}