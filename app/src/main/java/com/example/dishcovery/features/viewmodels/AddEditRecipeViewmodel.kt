package com.example.dishcovery.features.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditRecipeUiState(
    val recipeId: String? = null,
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
    val imageRes: Int? = null,
    val showImagePicker: Boolean = false,

    // Validation error states
    val imageError: String? = null,
    val recipeNameError: String? = null,
    val categoryError: String? = null,
    val ingredientsError: String? = null,
    val instructionsError: String? = null,
    val prepTimeError: String? = null,
    val cookTimeError: String? = null,
    val caloriesError: String? = null,
    val fatError: String? = null,
    val proteinError: String? = null,
    val carbsError: String? = null,
    val fiberError: String? = null,
    val sodiumError: String? = null,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val showValidationErrors: Boolean = false
)

class AddEditRecipeViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditRecipeUiState())
    val uiState: StateFlow<AddEditRecipeUiState> = _uiState.asStateFlow()

    val categories = listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Dessert")

    /**
     * Load a recipe from Firebase for editing
     */
    fun loadRecipe(recipeId: String?) {
        if (recipeId.isNullOrEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getRecipeById(recipeId)
                .onSuccess { recipe ->
                    if (recipe != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            recipeId = recipe.id,
                            recipeName = recipe.name,
                            selectedCategory = recipe.category,
                            ingredients = recipe.ingredients.ifEmpty { listOf("") },
                            instructions = recipe.instructions.ifEmpty { listOf("") },
                            prepTime = if (recipe.prepTime > 0) "${recipe.prepTime}" else "",
                            cookTime = if (recipe.cookTime > 0) "${recipe.cookTime}" else "",
                            calories = if (recipe.calories > 0) recipe.calories.toString() else "",
                            protein = if (recipe.protein > 0) recipe.protein.toString() else "",
                            carbs = if (recipe.carbs > 0) recipe.carbs.toString() else "",
                            fat = if (recipe.fat > 0) recipe.fat.toString() else "",
                            fiber = if (recipe.fiber > 0) recipe.fiber.toString() else "",
                            sodium = if (recipe.sodium > 0) recipe.sodium.toString() else "",
                            imageUri = recipe.imageUri,
                            imageRes = if (recipe.imageRes > 0) recipe.imageRes else null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Recipe not found"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load recipe: ${e.message}"
                    )
                }
        }
    }

    // ==================== Recipe Name ====================
    fun onRecipeNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            recipeName = name,
            recipeNameError = if (_uiState.value.showValidationErrors) {
                validateRecipeName(name)
            } else null
        )
    }

    private fun validateRecipeName(name: String): String? {
        val trimmedName = name.trim()
        return when {
            trimmedName.isEmpty() -> "Recipe name is required"
            trimmedName.length < 3 -> "Recipe name must be at least 3 characters"
            trimmedName.length > 100 -> "Recipe name must be less than 100 characters"
            !trimmedName[0].isLetter() -> "Recipe name must start with a letter"
            else -> null
        }
    }

    // ==================== Category ====================
    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            categoryError = null
        )
    }

    private fun validateCategory(category: String): String? {
        return if (category.isEmpty()) "Category is required" else null
    }

    // ==================== Ingredients ====================
    fun addIngredient() {
        val updatedIngredients = _uiState.value.ingredients + ""
        _uiState.value = _uiState.value.copy(
            ingredients = updatedIngredients,
            ingredientsError = if (_uiState.value.showValidationErrors) {
                validateIngredients(updatedIngredients)
            } else null
        )
    }

    fun updateIngredient(index: Int, value: String) {
        val updatedIngredients = _uiState.value.ingredients.toMutableList()
        if (index in updatedIngredients.indices) {
            updatedIngredients[index] = value
            _uiState.value = _uiState.value.copy(
                ingredients = updatedIngredients,
                ingredientsError = if (_uiState.value.showValidationErrors) {
                    validateIngredients(updatedIngredients)
                } else null
            )
        }
    }

    fun removeIngredient(index: Int) {
        val updatedIngredients = _uiState.value.ingredients.toMutableList()
        if (index in updatedIngredients.indices && updatedIngredients.size > 1) {
            updatedIngredients.removeAt(index)
            _uiState.value = _uiState.value.copy(
                ingredients = updatedIngredients,
                ingredientsError = if (_uiState.value.showValidationErrors) {
                    validateIngredients(updatedIngredients)
                } else null
            )
        }
    }

    private fun validateIngredients(ingredients: List<String>): String? {
        val nonEmptyIngredients = ingredients.filter { it.trim().isNotEmpty() }
        return when {
            nonEmptyIngredients.isEmpty() -> "At least one ingredient is required"
            nonEmptyIngredients.size < 2 -> "At least 2 ingredients are recommended"
            nonEmptyIngredients.any { it.trim().length < 2 } -> "Each ingredient must be at least 2 characters"
            nonEmptyIngredients.any { it.trim().length > 200 } -> "Ingredient too long (max 200 characters)"
            else -> null
        }
    }

    // ==================== Instructions ====================
    fun addInstruction() {
        val updatedInstructions = _uiState.value.instructions + ""
        _uiState.value = _uiState.value.copy(
            instructions = updatedInstructions,
            instructionsError = if (_uiState.value.showValidationErrors) {
                validateInstructions(updatedInstructions)
            } else null
        )
    }

    fun updateInstruction(index: Int, value: String) {
        val updatedInstructions = _uiState.value.instructions.toMutableList()
        if (index in updatedInstructions.indices) {
            updatedInstructions[index] = value
            _uiState.value = _uiState.value.copy(
                instructions = updatedInstructions,
                instructionsError = if (_uiState.value.showValidationErrors) {
                    validateInstructions(updatedInstructions)
                } else null
            )
        }
    }

    fun removeInstruction(index: Int) {
        val updatedInstructions = _uiState.value.instructions.toMutableList()
        if (index in updatedInstructions.indices && updatedInstructions.size > 1) {
            updatedInstructions.removeAt(index)
            _uiState.value = _uiState.value.copy(
                instructions = updatedInstructions,
                instructionsError = if (_uiState.value.showValidationErrors) {
                    validateInstructions(updatedInstructions)
                } else null
            )
        }
    }

    private fun validateInstructions(instructions: List<String>): String? {
        val nonEmptyInstructions = instructions.filter { it.trim().isNotEmpty() }
        return when {
            nonEmptyInstructions.isEmpty() -> "At least one instruction step is required"
            nonEmptyInstructions.any { it.trim().length < 10 } -> "Each instruction must be at least 10 characters"
            nonEmptyInstructions.any { it.trim().length > 500 } -> "Instruction too long (max 500 characters)"
            else -> null
        }
    }

    // ==================== Time Fields ====================
    fun onPrepTimeChange(time: String) {
        if (time.isEmpty() || time.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                prepTime = time,
                prepTimeError = if (_uiState.value.showValidationErrors) {
                    validateTime(time, "Prep time")
                } else null
            )
        }
    }

    fun onCookTimeChange(time: String) {
        if (time.isEmpty() || time.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                cookTime = time,
                cookTimeError = if (_uiState.value.showValidationErrors) {
                    validateTime(time, "Cook time")
                } else null
            )
        }
    }

    private fun validateTime(time: String, fieldName: String): String? {
        if (time.trim().isEmpty()) {
            return "$fieldName is required"
        }

        val timeValue = time.toIntOrNull()
        return when {
            timeValue == null -> "$fieldName must be a valid number"
            timeValue <= 0 -> "$fieldName must be greater than 0"
            timeValue > 1440 -> "$fieldName too long (max 1440 minutes)"
            else -> null
        }
    }

    // ==================== Nutrition Fields ====================
    fun onCaloriesChange(calories: String) {
        if (calories.isEmpty() || calories.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                calories = calories,
                caloriesError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(calories, "Calories", 0, 10000)
                } else null
            )
        }
    }

    fun onFatChange(fat: String) {
        if (fat.isEmpty() || fat.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                fat = fat,
                fatError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(fat, "Fat", 0.0, 500.0)
                } else null
            )
        }
    }

    fun onProteinChange(protein: String) {
        if (protein.isEmpty() || protein.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                protein = protein,
                proteinError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(protein, "Protein", 0.0, 500.0)
                } else null
            )
        }
    }

    fun onCarbsChange(carbs: String) {
        if (carbs.isEmpty() || carbs.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                carbs = carbs,
                carbsError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(carbs, "Carbs", 0.0, 1000.0)
                } else null
            )
        }
    }

    fun onFiberChange(fiber: String) {
        if (fiber.isEmpty() || fiber.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                fiber = fiber,
                fiberError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(fiber, "Fiber", 0.0, 100.0)
                } else null
            )
        }
    }

    fun onSodiumChange(sodium: String) {
        if (sodium.isEmpty() || sodium.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                sodium = sodium,
                sodiumError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(sodium, "Sodium", 0, 10000)
                } else null
            )
        }
    }

    private fun validateNutrition(value: String, fieldName: String, min: Number, max: Number): String? {
        if (value.trim().isEmpty()) {
            return "$fieldName is required"
        }

        val numValue = value.toDoubleOrNull()
        return when {
            numValue == null -> "$fieldName must be a valid number"
            numValue < min.toDouble() -> "$fieldName cannot be negative"
            numValue > max.toDouble() -> "$fieldName too high (max ${max})"
            else -> null
        }
    }

    private fun validateImage(): String? {
        return if (_uiState.value.imageUri == null && _uiState.value.imageRes == null) {
            "Please upload a photo of your recipe"
        } else {
            null
        }
    }

    // ==================== Image ====================
    fun onImageSelected(uri: String?) {
        _uiState.value = _uiState.value.copy(
            imageUri = uri,
            showImagePicker = false,
            imageError = null
        )
    }

    fun showImagePicker() {
        _uiState.value = _uiState.value.copy(showImagePicker = true)
    }

    fun hideImagePicker() {
        _uiState.value = _uiState.value.copy(showImagePicker = false)
    }

    fun removeImage() {
        _uiState.value = _uiState.value.copy(
            imageUri = null,
            imageRes = null
        )
    }

    // ==================== Form Submission ====================
    fun saveRecipe(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(showValidationErrors = true)

        // Validate all fields
        val nameError = validateRecipeName(_uiState.value.recipeName)
        val categoryError = validateCategory(_uiState.value.selectedCategory)
        val ingredientsError = validateIngredients(_uiState.value.ingredients)
        val instructionsError = validateInstructions(_uiState.value.instructions)
        val prepTimeError = validateTime(_uiState.value.prepTime, "Prep time")
        val cookTimeError = validateTime(_uiState.value.cookTime, "Cook time")
        val caloriesError = validateNutrition(_uiState.value.calories, "Calories", 0, 10000)
        val fatError = validateNutrition(_uiState.value.fat, "Fat", 0.0, 500.0)
        val proteinError = validateNutrition(_uiState.value.protein, "Protein", 0.0, 500.0)
        val carbsError = validateNutrition(_uiState.value.carbs, "Carbs", 0.0, 1000.0)
        val fiberError = validateNutrition(_uiState.value.fiber, "Fiber", 0.0, 100.0)
        val sodiumError = validateNutrition(_uiState.value.sodium, "Sodium", 0, 10000)
        val imageError = validateImage()

        _uiState.value = _uiState.value.copy(
            recipeNameError = nameError,
            categoryError = categoryError,
            ingredientsError = ingredientsError,
            instructionsError = instructionsError,
            prepTimeError = prepTimeError,
            cookTimeError = cookTimeError,
            caloriesError = caloriesError,
            fatError = fatError,
            proteinError = proteinError,
            carbsError = carbsError,
            fiberError = fiberError,
            sodiumError = sodiumError,
            imageError = imageError
        )

        val hasErrors = listOf(
            nameError, categoryError, ingredientsError, instructionsError,
            prepTimeError, cookTimeError, caloriesError, fatError,
            proteinError, carbsError, fiberError, sodiumError, imageError
        ).any { it != null }

        if (hasErrors) {
            _uiState.value = _uiState.value.copy(
                error = "Please fix the errors before saving"
            )
            return
        }

        // Save to Firebase
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            try {
                val recipe = Recipe(
                    id = _uiState.value.recipeId ?: "",
                    name = _uiState.value.recipeName.trim(),
                    category = _uiState.value.selectedCategory,
                    ingredients = _uiState.value.ingredients.filter { it.trim().isNotEmpty() },
                    instructions = _uiState.value.instructions.filter { it.trim().isNotEmpty() },
                    prepTime = _uiState.value.prepTime.toIntOrNull() ?: 0,
                    cookTime = _uiState.value.cookTime.toIntOrNull() ?: 0,
                    calories = _uiState.value.calories.toIntOrNull() ?: 0,
                    protein = _uiState.value.protein.toDoubleOrNull()?.toInt() ?: 0,
                    carbs = _uiState.value.carbs.toDoubleOrNull()?.toInt() ?: 0,
                    fat = _uiState.value.fat.toDoubleOrNull()?.toInt() ?: 0,
                    fiber = _uiState.value.fiber.toDoubleOrNull()?.toInt() ?: 0,
                    sodium = _uiState.value.sodium.toIntOrNull() ?: 0,
                    imageRes = _uiState.value.imageRes ?: 0,
                    imageUri = _uiState.value.imageUri,
                    updatedAt = System.currentTimeMillis()
                )

                // Add or update recipe
                val result = if (_uiState.value.recipeId.isNullOrEmpty()) {
                    repository.addRecipe(recipe)
                } else {
                    repository.updateRecipe(_uiState.value.recipeId!!, recipe)
                        .map { "" } // Convert Unit to String for consistency
                }

                result
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            saveSuccess = true
                        )
                        onSuccess()
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = "Failed to save recipe: ${e.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save recipe: ${e.message}"
                )
            }
        }
    }

    fun resetRecipe() {
        _uiState.value = AddEditRecipeUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun hasUnsavedChanges(originalRecipe: Recipe?): Boolean {
        if (originalRecipe == null) {
            return _uiState.value.recipeName.isNotEmpty() ||
                    _uiState.value.ingredients.any { it.trim().isNotEmpty() } ||
                    _uiState.value.instructions.any { it.trim().isNotEmpty() }
        }

        return _uiState.value.recipeName.trim() != originalRecipe.name ||
                _uiState.value.selectedCategory != originalRecipe.category ||
                _uiState.value.ingredients.filter { it.trim().isNotEmpty() } != originalRecipe.ingredients ||
                _uiState.value.instructions.filter { it.trim().isNotEmpty() } != originalRecipe.instructions
    }
}