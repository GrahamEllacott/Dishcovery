package com.example.dishcovery.features.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.R
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.repository.RecipeRepository
import com.example.dishcovery.util.RecipeCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

class RecipeDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository = RecipeRepository(application)

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: String) {
        Log.d("RecipeDetailViewModel", "Loading recipe with ID: $recipeId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Check if this is a temp/API recipe
            if (recipeId.startsWith("temp_")) {
                Log.d("RecipeDetailViewModel", "This is a temp recipe, loading from cache")
                val cachedRecipe = RecipeCache.get(recipeId)

                if (cachedRecipe != null) {
                    Log.d("RecipeDetailViewModel", "Found cached recipe: ${cachedRecipe.name}")
                    _uiState.value = _uiState.value.copy(
                        recipe = cachedRecipe,
                        isLoading = false,
                        error = null
                    )
                } else {
                    Log.e("RecipeDetailViewModel", "Cached recipe not found for ID: $recipeId")
                    _uiState.value = _uiState.value.copy(
                        recipe = null,
                        error = getApplication<Application>().getString(R.string.error_recipe_not_found_cache),
                        isLoading = false
                    )
                }
                return@launch
            }

            // Load from Firebase for non-temp recipes
            Log.d("RecipeDetailViewModel", "Loading from Firebase")
            repository.getRecipeById(recipeId)
                .onSuccess { recipe ->
                    if (recipe != null) {
                        Log.d("RecipeDetailViewModel", "Loaded recipe from Firebase: ${recipe.name}")
                        _uiState.value = _uiState.value.copy(
                            recipe = recipe,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        Log.e("RecipeDetailViewModel", "Recipe not found in Firebase")
                        _uiState.value = _uiState.value.copy(
                            recipe = null,
                            error = getApplication<Application>().getString(R.string.error_recipe_not_found),
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("RecipeDetailViewModel", "Failed to load recipe: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        recipe = null,
                        error = getApplication<Application>().getString(R.string.error_failed_load_recipe, e.message ?: "Unknown error"),
                        isLoading = false
                    )
                }
        }
    }

    fun saveRecipeToFirebase(recipe: Recipe) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            try {
                // Create a new recipe without the temp ID
                val recipeToSave = recipe.copy(
                    id = "", // Firebase will generate a new ID
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    checkedIngredients = emptyList(), // Reset checkboxes for new recipe
                    checkedInstructions = emptyList()
                )

                repository.addRecipe(recipeToSave)
                    .onSuccess { newRecipeId ->
                        Log.d("RecipeDetailViewModel", "Recipe saved successfully with ID: $newRecipeId")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            saveSuccess = true
                        )
                    }
                    .onFailure { e ->
                        Log.e("RecipeDetailViewModel", "Failed to save recipe: ${e.message}", e)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = getApplication<Application>().getString(R.string.error_failed_save_recipe, e.message ?: "Unknown error")
                        )
                    }
            } catch (e: Exception) {
                Log.e("RecipeDetailViewModel", "Exception while saving recipe: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = getApplication<Application>().getString(R.string.error_generic, e.message ?: "Unknown error")
                )
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)

            try {
                repository.deleteRecipe(recipeId)
                    .onSuccess {
                        Log.d("RecipeDetailViewModel", "Recipe deleted successfully")
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            deleteSuccess = true
                        )
                    }
                    .onFailure { e ->
                        Log.e("RecipeDetailViewModel", "Failed to delete recipe: ${e.message}", e)
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            error = getApplication<Application>().getString(R.string.error_failed_delete_recipe, e.message ?: "Unknown error")
                        )
                    }
            } catch (e: Exception) {
                Log.e("RecipeDetailViewModel", "Exception while deleting recipe: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = getApplication<Application>().getString(R.string.error_generic, e.message ?: "Unknown error")
                )
            }
        }
    }

    fun toggleIngredient(index: Int) {
        val recipe = _uiState.value.recipe ?: return

        // For temp/API recipes, update in cache only
        if (recipe.id.startsWith("temp_")) {
            val updatedCheckedIngredients = recipe.checkedIngredients.toMutableList()
            while (updatedCheckedIngredients.size < recipe.ingredients.size) {
                updatedCheckedIngredients.add(false)
            }
            if (index in updatedCheckedIngredients.indices) {
                updatedCheckedIngredients[index] = !updatedCheckedIngredients[index]
            }

            val updatedRecipe = recipe.copy(checkedIngredients = updatedCheckedIngredients)
            RecipeCache.put(recipe.id, updatedRecipe)
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
                        error = getApplication<Application>().getString(R.string.error_failed_update, e.message ?: "Unknown error")
                    )
                }
        }
    }

    fun toggleInstruction(index: Int) {
        val recipe = _uiState.value.recipe ?: return

        // For temp/API recipes, update in cache only
        if (recipe.id.startsWith("temp_")) {
            val updatedCheckedInstructions = recipe.checkedInstructions.toMutableList()
            while (updatedCheckedInstructions.size < recipe.instructions.size) {
                updatedCheckedInstructions.add(false)
            }
            if (index in updatedCheckedInstructions.indices) {
                updatedCheckedInstructions[index] = !updatedCheckedInstructions[index]
            }

            val updatedRecipe = recipe.copy(checkedInstructions = updatedCheckedInstructions)
            RecipeCache.put(recipe.id, updatedRecipe)
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
                        error = getApplication<Application>().getString(R.string.error_failed_update, e.message ?: "Unknown error")
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }
}