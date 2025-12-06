package com.example.dishcovery.features.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.MealPlanDBHelper
import com.example.dishcovery.data.models.MealPlan
import com.example.dishcovery.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeeklyMealPlanUiState(
    val week: LocalDate? = null,
    val mealPlans: List<MealPlan> = emptyList()
)

class WeeklyMealPlanViewModel(application: Application) : AndroidViewModel(application){
    private val repository: RecipeRepository = RecipeRepository(application)
    private val mealPlanDBHelper: MealPlanDBHelper = MealPlanDBHelper(application)
    private val _uiState = MutableStateFlow(WeeklyMealPlanUiState())
    val uiState: StateFlow<WeeklyMealPlanUiState> = _uiState.asStateFlow()


    init {
        setCurrentWeek()
        getMealPlans()
    }

    private fun setCurrentWeek(){
        // get the start of this week
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

    private fun getMealPlans() {viewModelScope.launch { // Launch a single coroutine for the whole operation
        val mealPlansForWeek = (0..6).map { i ->
            val date = _uiState.value.week!!.plusDays(i.toLong())
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
                // Return a new MealPlan object with the fetched recipes included
                mealPlan.copy(recipes = recipes)
            } else {
                // If no recipes, return the mealPlan as is
                mealPlan
            }
        }
        // Now, update the state once with the complete data
        _uiState.value = _uiState.value.copy(mealPlans = mealPlansForWeek)
    }
    }

}