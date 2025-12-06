package com.example.dishcovery.features.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditRecipeUiState(
    val recipeId: Int? = null,
    val recipeName: String = "",
    val selectedCategory: String = "Dinner",
    val ingredients: List<String> = listOf(""),
    val instructions: List<String> = listOf(""),
    val prepTime: String = "",
    val cookTime: String = "",
    val calories: String = "",
    val fat: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fiber: String = "",
    val sodium: String = "",
    val imageUri: String? = null,
    val showNameError: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

class AddEditRecipeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditRecipeUiState())
    val uiState: StateFlow<AddEditRecipeUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Int?) {
        if (recipeId != null) {
            viewModelScope.launch {
                // TODO: Load recipe from repository
                val recipe = getRecipeById(recipeId)
                _uiState.value = _uiState.value.copy(
                    recipeId = recipe.id,
                    recipeName = recipe.name,
                    selectedCategory = recipe.category,
                    ingredients = recipe.ingredients,
                    instructions = recipe.instructions,
                    prepTime = recipe.prepTime.toString(),
                    cookTime = recipe.cookTime.toString(),
                    calories = recipe.calories.toString(),
                    fat = recipe.fat.toString()
                )
            }
        }
    }

    fun onRecipeNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            recipeName = name,
            showNameError = name.isEmpty()
        )
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun addIngredient() {
        val updatedIngredients = _uiState.value.ingredients + ""
        _uiState.value = _uiState.value.copy(ingredients = updatedIngredients)
    }

    fun updateIngredient(index: Int, value: String) {
        val updatedIngredients = _uiState.value.ingredients.toMutableList()
        updatedIngredients[index] = value
        _uiState.value = _uiState.value.copy(ingredients = updatedIngredients)
    }

    fun removeIngredient(index: Int) {
        val updatedIngredients = _uiState.value.ingredients.toMutableList()
        updatedIngredients.removeAt(index)
        _uiState.value = _uiState.value.copy(ingredients = updatedIngredients)
    }

    fun addInstruction() {
        val updatedInstructions = _uiState.value.instructions + ""
        _uiState.value = _uiState.value.copy(instructions = updatedInstructions)
    }

    fun updateInstruction(index: Int, value: String) {
        val updatedInstructions = _uiState.value.instructions.toMutableList()
        updatedInstructions[index] = value
        _uiState.value = _uiState.value.copy(instructions = updatedInstructions)
    }

    fun removeInstruction(index: Int) {
        val updatedInstructions = _uiState.value.instructions.toMutableList()
        updatedInstructions.removeAt(index)
        _uiState.value = _uiState.value.copy(instructions = updatedInstructions)
    }

    fun onPrepTimeChange(time: String) {
        _uiState.value = _uiState.value.copy(prepTime = time)
    }

    fun onCookTimeChange(time: String) {
        _uiState.value = _uiState.value.copy(cookTime = time)
    }

    fun onCaloriesChange(calories: String) {
        _uiState.value = _uiState.value.copy(calories = calories)
    }

    fun onFatChange(fat: String) {
        _uiState.value = _uiState.value.copy(fat = fat)
    }

    fun saveRecipe(onSuccess: () -> Unit) {
        if (_uiState.value.recipeName.isEmpty()) {
            _uiState.value = _uiState.value.copy(showNameError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            // TODO: Save to repository
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSuccess()
        }
    }

    fun resetRecipe() {
        _uiState.value = AddEditRecipeUiState()
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
            category = "Dinner",
            ingredients = listOf("400g spaghetti", "200g pancetta"),
            instructions = listOf("Boil water", "Cook pasta")
        )
    }
}
