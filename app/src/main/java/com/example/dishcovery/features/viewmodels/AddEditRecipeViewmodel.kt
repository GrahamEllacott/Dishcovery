package com.example.dishcovery.features.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.R
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

class AddEditRecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository = RecipeRepository(application)

    private val _uiState = MutableStateFlow(AddEditRecipeUiState())
    val uiState: StateFlow<AddEditRecipeUiState> = _uiState.asStateFlow()

    val categories = listOf(
        getApplication<Application>().getString(R.string.category_breakfast),
        getApplication<Application>().getString(R.string.category_lunch),
        getApplication<Application>().getString(R.string.category_dinner),
        getApplication<Application>().getString(R.string.category_snacks),
        getApplication<Application>().getString(R.string.category_dessert)
    )

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
                            imageRes = if (recipe.imageRes > 0) recipe.imageRes else null,
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = getApplication<Application>().getString(R.string.error_recipe_not_found)
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = getApplication<Application>().getString(R.string.error_failed_load_recipe, e.message ?: "Unknown error")
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
        val context = getApplication<Application>()
        return when {
            trimmedName.isEmpty() -> context.getString(R.string.error_recipe_name_required)
            trimmedName.length < 3 -> context.getString(R.string.error_recipe_name_min_length)
            trimmedName.length > 100 -> context.getString(R.string.error_recipe_name_max_length)
            !trimmedName[0].isLetter() -> context.getString(R.string.error_recipe_name_start_letter)
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
        val context = getApplication<Application>()
        return if (category.isEmpty()) context.getString(R.string.error_category_required) else null
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
        val context = getApplication<Application>()
        return when {
            nonEmptyIngredients.isEmpty() -> context.getString(R.string.error_ingredients_required)
            nonEmptyIngredients.size < 2 -> context.getString(R.string.error_ingredients_min_count)
            nonEmptyIngredients.any { it.trim().length < 2 } -> context.getString(R.string.error_ingredient_min_length)
            nonEmptyIngredients.any { it.trim().length > 200 } -> context.getString(R.string.error_ingredient_max_length)
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
        val context = getApplication<Application>()
        return when {
            nonEmptyInstructions.isEmpty() -> context.getString(R.string.error_instructions_required)
            nonEmptyInstructions.any { it.trim().length < 10 } -> context.getString(R.string.error_instruction_min_length)
            nonEmptyInstructions.any { it.trim().length > 500 } -> context.getString(R.string.error_instruction_max_length)
            else -> null
        }
    }

    // ==================== Time Fields ====================
    fun onPrepTimeChange(time: String) {
        if (time.isEmpty() || time.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                prepTime = time,
                prepTimeError = if (_uiState.value.showValidationErrors) {
                    validateTime(time, getApplication<Application>().getString(R.string.field_prep_time))
                } else null
            )
        }
    }

    fun onCookTimeChange(time: String) {
        if (time.isEmpty() || time.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                cookTime = time,
                cookTimeError = if (_uiState.value.showValidationErrors) {
                    validateTime(time, getApplication<Application>().getString(R.string.field_cook_time))
                } else null
            )
        }
    }

    private fun validateTime(time: String, fieldName: String): String? {
        val context = getApplication<Application>()
        if (time.trim().isEmpty()) {
            return context.getString(R.string.error_time_required, fieldName)
        }

        val timeValue = time.toIntOrNull()
        return when {
            timeValue == null -> context.getString(R.string.error_time_invalid, fieldName)
            timeValue <= 0 || timeValue > 1440 -> context.getString(R.string.error_time_range, fieldName, 1, 1440)
            else -> null
        }
    }

    // ==================== Nutrition Fields ====================
    fun onCaloriesChange(calories: String) {
        if (calories.isEmpty() || calories.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                calories = calories,
                caloriesError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(calories, getApplication<Application>().getString(R.string.field_calories), 0, 10000)
                } else null
            )
        }
    }

    fun onFatChange(fat: String) {
        if (fat.isEmpty() || fat.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                fat = fat,
                fatError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(fat, getApplication<Application>().getString(R.string.field_fat), 0.0, 500.0)
                } else null
            )
        }
    }

    fun onProteinChange(protein: String) {
        if (protein.isEmpty() || protein.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                protein = protein,
                proteinError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(protein, getApplication<Application>().getString(R.string.field_protein), 0.0, 500.0)
                } else null
            )
        }
    }

    fun onCarbsChange(carbs: String) {
        if (carbs.isEmpty() || carbs.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                carbs = carbs,
                carbsError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(carbs, getApplication<Application>().getString(R.string.field_carbs), 0.0, 1000.0)
                } else null
            )
        }
    }

    fun onFiberChange(fiber: String) {
        if (fiber.isEmpty() || fiber.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                fiber = fiber,
                fiberError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(fiber, getApplication<Application>().getString(R.string.field_fiber), 0.0, 100.0)
                } else null
            )
        }
    }

    fun onSodiumChange(sodium: String) {
        if (sodium.isEmpty() || sodium.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                sodium = sodium,
                sodiumError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(sodium, getApplication<Application>().getString(R.string.field_sodium), 0, 10000)
                } else null
            )
        }
    }

    private fun validateNutrition(value: String, fieldName: String, min: Number, max: Number): String? {
        val context = getApplication<Application>()
        if (value.trim().isEmpty()) {
            return context.getString(R.string.error_nutrition_required, fieldName)
        }

        val numValue = value.toDoubleOrNull()
        return when {
            numValue == null -> context.getString(R.string.error_nutrition_invalid, fieldName)
            numValue < min.toDouble() || numValue > max.toDouble() -> {
                if (min is Int && max is Int) {
                    context.getString(R.string.error_nutrition_range_int, fieldName, min, max)
                } else {
                    context.getString(R.string.error_nutrition_range_double, fieldName, min.toDouble(), max.toDouble())
                }
            }
            else -> null
        }
    }

    private fun validateImage(): String? {
        val context = getApplication<Application>()
        return if (_uiState.value.imageUri == null && _uiState.value.imageRes == null) {
            context.getString(R.string.error_image_required)
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
        val context = getApplication<Application>()
        val prepTimeError = validateTime(_uiState.value.prepTime, context.getString(R.string.field_prep_time))
        val cookTimeError = validateTime(_uiState.value.cookTime, context.getString(R.string.field_cook_time))
        val caloriesError = validateNutrition(_uiState.value.calories, context.getString(R.string.field_calories), 0, 10000)
        val fatError = validateNutrition(_uiState.value.fat, context.getString(R.string.field_fat), 0.0, 500.0)
        val proteinError = validateNutrition(_uiState.value.protein, context.getString(R.string.field_protein), 0.0, 500.0)
        val carbsError = validateNutrition(_uiState.value.carbs, context.getString(R.string.field_carbs), 0.0, 1000.0)
        val fiberError = validateNutrition(_uiState.value.fiber, context.getString(R.string.field_fiber), 0.0, 100.0)
        val sodiumError = validateNutrition(_uiState.value.sodium, context.getString(R.string.field_sodium), 0, 10000)
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
                error = getApplication<Application>().getString(R.string.error_fix_errors_before_saving)
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
                    updatedAt = System.currentTimeMillis(),
                    checkedIngredients = List(_uiState.value.ingredients.filter { it.trim().isNotEmpty() }.size) { false },
                    checkedInstructions = List(_uiState.value.instructions.filter { it.trim().isNotEmpty() }.size) { false },
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
                            error = getApplication<Application>().getString(R.string.error_failed_save_recipe, e.message ?: "Unknown error")
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = getApplication<Application>().getString(R.string.error_failed_save_recipe, e.message ?: "Unknown error")
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