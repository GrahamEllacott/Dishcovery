package com.example.dishcovery.features.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipeDetailViewModel : ViewModel() {

    private lateinit var repository: RecipeRepository

    // Add this to receive cached recipe
    private var cachedRecipe: Recipe? = null

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun initRepository(context: Context) {
        repository = RecipeRepository(context)
    }

    // Add method to set cached recipe
    fun setCachedRecipe(recipe: Recipe) {
        cachedRecipe = recipe
        _uiState.value = _uiState.value.copy(recipe = recipe)
    }

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // If this is a temporary ID (from API), use cached recipe
            if (recipeId.startsWith("temp_") && cachedRecipe != null) {
                _uiState.value = _uiState.value.copy(
                    recipe = cachedRecipe,
                    isLoading = false
                )
                return@launch
            }

            // Otherwise load from Firebase
            repository.getRecipeById(recipeId)
                .onSuccess { recipe ->
                    _uiState.value = _uiState.value.copy(
                        recipe = recipe,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load recipe: ${e.message}",
                        isLoading = false
                    )
                }
        }
    }

    fun toggleIngredient(index: Int) {
        val recipe = _uiState.value.recipe ?: return

        // If this is an API recipe (temp ID), just update locally
        if (recipe.id.startsWith("temp_")) {
            val updatedCheckedIngredients = recipe.checkedIngredients.toMutableList()
            while (updatedCheckedIngredients.size < recipe.ingredients.size) {
                updatedCheckedIngredients.add(false)
            }
            if (index in updatedCheckedIngredients.indices) {
                updatedCheckedIngredients[index] = !updatedCheckedIngredients[index]
            }

            val updatedRecipe = recipe.copy(checkedIngredients = updatedCheckedIngredients)
            cachedRecipe = updatedRecipe
            _uiState.value = _uiState.value.copy(recipe = updatedRecipe)
            return
        }

        // For Firebase recipes, persist the change
        viewModelScope.launch {
            val currentChecked = recipe.checkedIngredients.getOrElse(index) { false }
            val newChecked = !currentChecked

            // Update UI immediately
            val updatedCheckedIngredients = recipe.checkedIngredients.toMutableList()
            while (updatedCheckedIngredients.size < recipe.ingredients.size) {
                updatedCheckedIngredients.add(false)
            }
            if (index in updatedCheckedIngredients.indices) {
                updatedCheckedIngredients[index] = newChecked
            }

            _uiState.value = _uiState.value.copy(
                recipe = recipe.copy(checkedIngredients = updatedCheckedIngredients)
            )

            // Persist to Firebase
            repository.toggleIngredient(recipe.id, index, newChecked)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${e.message}"
                    )
                }
        }
    }

    fun toggleInstruction(index: Int) {
        val recipe = _uiState.value.recipe ?: return

        // If this is an API recipe (temp ID), just update locally
        if (recipe.id.startsWith("temp_")) {
            val updatedCheckedInstructions = recipe.checkedInstructions.toMutableList()
            while (updatedCheckedInstructions.size < recipe.instructions.size) {
                updatedCheckedInstructions.add(false)
            }
            if (index in updatedCheckedInstructions.indices) {
                updatedCheckedInstructions[index] = !updatedCheckedInstructions[index]
            }

            val updatedRecipe = recipe.copy(checkedInstructions = updatedCheckedInstructions)
            cachedRecipe = updatedRecipe
            _uiState.value = _uiState.value.copy(recipe = updatedRecipe)
            return
        }

        // For Firebase recipes, persist the change
        viewModelScope.launch {
            val currentChecked = recipe.checkedInstructions.getOrElse(index) { false }
            val newChecked = !currentChecked

            // Update UI immediately
            val updatedCheckedInstructions = recipe.checkedInstructions.toMutableList()
            while (updatedCheckedInstructions.size < recipe.instructions.size) {
                updatedCheckedInstructions.add(false)
            }
            if (index in updatedCheckedInstructions.indices) {
                updatedCheckedInstructions[index] = newChecked
            }

            _uiState.value = _uiState.value.copy(
                recipe = recipe.copy(checkedInstructions = updatedCheckedInstructions)
            )

            // Persist to Firebase
            repository.toggleInstruction(recipe.id, index, newChecked)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${e.message}"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}