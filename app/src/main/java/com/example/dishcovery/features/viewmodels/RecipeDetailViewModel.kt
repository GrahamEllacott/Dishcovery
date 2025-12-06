package com.example.dishcovery.features.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)

class RecipeDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // TODO: Replace with actual repository call
            val recipe = getRecipeById(recipeId)
            _uiState.value = _uiState.value.copy(
                recipe = recipe,
                isLoading = false
            )
        }
    }

    fun toggleFavorite() {
        _uiState.value.recipe?.let { recipe ->
            val updatedRecipe = recipe.copy(isFavorite = !recipe.isFavorite)
            _uiState.value = _uiState.value.copy(recipe = updatedRecipe)
            // TODO: Update in repository
        }
    }

    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun deleteRecipe() {
        viewModelScope.launch {
            // TODO: Delete from repository
            hideDeleteDialog()
        }
    }

    fun addToMealPlan() {
        // TODO: Add to meal plan
    }

    private fun getRecipeById(id: Int): Recipe {
        // TODO: Replace with actual repository call
        return Recipe(
            id = id,
            name = "Spaghetti Carbonara",
            imageRes = com.example.dishcovery.R.drawable.ic_launcher_background,
            prepTime = 15,
            cookTime = 10,
            calories = 520,
            fat = 19,
            protein = 28,
            carbs = 58,
            fiber = 3,
            sodium = 680,
            category = "Dinner",
            ingredients = listOf(
                "400g spaghetti",
                "200g pancetta or bacon, diced",
                "4 large eggs",
                "100g Parmesan cheese, grated",
                "2 cloves garlic, minced",
                "Salt and black pepper to taste",
                "Fresh parsley for garnish"
            ),
            instructions = listOf(
                "Bring a large pot of salted water to boil. Cook spaghetti according to package directions until al dente.",
                "While pasta cooks, heat a large skillet over medium heat. Add pancetta and cook until crispy, about 5-7 minutes.",
                "In a bowl, whisk together eggs, Parmesan cheese, and a generous amount of black pepper.",
                "Drain pasta, reserving 1 cup of pasta water. Add hot pasta to the skillet with pancetta.",
                "Remove from heat and quickly stir in the egg mixture, tossing constantly. Add pasta water as needed to create a creamy sauce.",
                "Season with salt and more pepper. Garnish with parsley and extra Parmesan."
            )
        )
    }
}