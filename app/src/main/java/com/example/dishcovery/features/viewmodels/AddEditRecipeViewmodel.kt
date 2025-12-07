package com.example.dishcovery.features.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.R
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.repository.RecipeRepository
import com.example.dishcovery.util.ValidationResult
import com.example.dishcovery.util.getErrorOrNull
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
                validateRecipeName(name).getErrorOrNull()
            } else null
        )
    }

    /**
     * Validates recipe name using Sealed Class pattern
     * @return ValidationResult.Valid if valid, ValidationResult.Invalid with error message if invalid
     */
    private fun validateRecipeName(name: String): ValidationResult {
        val trimmedName = name.trim()
        val context = getApplication<Application>()
        return when {
            trimmedName.isEmpty() -> ValidationResult.Invalid(context.getString(R.string.error_recipe_name_required))
            trimmedName.length < 3 -> ValidationResult.Invalid(context.getString(R.string.error_recipe_name_min_length))
            trimmedName.length > 100 -> ValidationResult.Invalid(context.getString(R.string.error_recipe_name_max_length))
            !trimmedName[0].isLetter() -> ValidationResult.Invalid(context.getString(R.string.error_recipe_name_start_letter))
            else -> ValidationResult.Valid
        }
    }

    // ==================== Category ====================
    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            categoryError = null
        )
    }

    /**
     * Validates category selection using Sealed Class pattern
     * @return ValidationResult.Valid if valid, ValidationResult.Invalid with error message if invalid
     */
    private fun validateCategory(category: String): ValidationResult {
        val context = getApplication<Application>()
        return if (category.isEmpty()) {
            ValidationResult.Invalid(context.getString(R.string.error_category_required))
        } else {
            ValidationResult.Valid
        }
    }

    // ==================== Ingredients ====================
    fun addIngredient() {
        val updatedIngredients = _uiState.value.ingredients + ""
        _uiState.value = _uiState.value.copy(
            ingredients = updatedIngredients,
            ingredientsError = if (_uiState.value.showValidationErrors) {
                validateIngredients(updatedIngredients).getErrorOrNull()
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
                    validateIngredients(updatedIngredients).getErrorOrNull()
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
                    validateIngredients(updatedIngredients).getErrorOrNull()
                } else null
            )
        }
    }

    /**
     * Validates ingredients list using Sealed Class pattern
     * @return ValidationResult.Valid if valid, ValidationResult.Invalid with error message if invalid
     */
    private fun validateIngredients(ingredients: List<String>): ValidationResult {
        val nonEmptyIngredients = ingredients.filter { it.trim().isNotEmpty() }
        val context = getApplication<Application>()
        return when {
            nonEmptyIngredients.isEmpty() -> ValidationResult.Invalid(context.getString(R.string.error_ingredients_required))
            nonEmptyIngredients.size < 2 -> ValidationResult.Invalid(context.getString(R.string.error_ingredients_min_count))
            nonEmptyIngredients.any { it.trim().length < 2 } -> ValidationResult.Invalid(context.getString(R.string.error_ingredient_min_length))
            nonEmptyIngredients.any { it.trim().length > 200 } -> ValidationResult.Invalid(context.getString(R.string.error_ingredient_max_length))
            else -> ValidationResult.Valid
        }
    }

    // ==================== Instructions ====================
    fun addInstruction() {
        val updatedInstructions = _uiState.value.instructions + ""
        _uiState.value = _uiState.value.copy(
            instructions = updatedInstructions,
            instructionsError = if (_uiState.value.showValidationErrors) {
                validateInstructions(updatedInstructions).getErrorOrNull()
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
                    validateInstructions(updatedInstructions).getErrorOrNull()
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
                    validateInstructions(updatedInstructions).getErrorOrNull()
                } else null
            )
        }
    }

    /**
     * Validates instructions list using Sealed Class pattern
     * @return ValidationResult.Valid if valid, ValidationResult.Invalid with error message if invalid
     */
    private fun validateInstructions(instructions: List<String>): ValidationResult {
        val nonEmptyInstructions = instructions.filter { it.trim().isNotEmpty() }
        val context = getApplication<Application>()
        return when {
            nonEmptyInstructions.isEmpty() -> ValidationResult.Invalid(context.getString(R.string.error_instructions_required))
            nonEmptyInstructions.any { it.trim().length < 10 } -> ValidationResult.Invalid(context.getString(R.string.error_instruction_min_length))
            nonEmptyInstructions.any { it.trim().length > 500 } -> ValidationResult.Invalid(context.getString(R.string.error_instruction_max_length))
            else -> ValidationResult.Valid
        }
    }

    // ==================== Time Fields ====================
    fun onPrepTimeChange(time: String) {
        if (time.isEmpty() || time.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                prepTime = time,
                prepTimeError = if (_uiState.value.showValidationErrors) {
                    validateTime(time, getApplication<Application>().getString(R.string.field_prep_time)).getErrorOrNull()
                } else null
            )
        }
    }

    fun onCookTimeChange(time: String) {
        if (time.isEmpty() || time.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                cookTime = time,
                cookTimeError = if (_uiState.value.showValidationErrors) {
                    validateTime(time, getApplication<Application>().getString(R.string.field_cook_time)).getErrorOrNull()
                } else null
            )
        }
    }

    /**
     * Validates time field using Sealed Class pattern
     * @return ValidationResult.Valid if valid, ValidationResult.Invalid with error message if invalid
     */
    private fun validateTime(time: String, fieldName: String): ValidationResult {
        val context = getApplication<Application>()
        if (time.trim().isEmpty()) {
            return ValidationResult.Invalid(context.getString(R.string.error_time_required, fieldName))
        }

        val timeValue = time.toIntOrNull()
        return when {
            timeValue == null -> ValidationResult.Invalid(context.getString(R.string.error_time_invalid, fieldName))
            timeValue <= 0 || timeValue > 1440 -> ValidationResult.Invalid(context.getString(R.string.error_time_range, fieldName, 1, 1440))
            else -> ValidationResult.Valid
        }
    }

    // ==================== Nutrition Fields ====================
    fun onCaloriesChange(calories: String) {
        if (calories.isEmpty() || calories.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                calories = calories,
                caloriesError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(calories, getApplication<Application>().getString(R.string.field_calories), 0, 10000).getErrorOrNull()
                } else null
            )
        }
    }

    fun onFatChange(fat: String) {
        if (fat.isEmpty() || fat.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                fat = fat,
                fatError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(fat, getApplication<Application>().getString(R.string.field_fat), 0.0, 500.0).getErrorOrNull()
                } else null
            )
        }
    }

    fun onProteinChange(protein: String) {
        if (protein.isEmpty() || protein.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                protein = protein,
                proteinError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(protein, getApplication<Application>().getString(R.string.field_protein), 0.0, 500.0).getErrorOrNull()
                } else null
            )
        }
    }

    fun onCarbsChange(carbs: String) {
        if (carbs.isEmpty() || carbs.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                carbs = carbs,
                carbsError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(carbs, getApplication<Application>().getString(R.string.field_carbs), 0.0, 1000.0).getErrorOrNull()
                } else null
            )
        }
    }

    fun onFiberChange(fiber: String) {
        if (fiber.isEmpty() || fiber.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(
                fiber = fiber,
                fiberError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(fiber, getApplication<Application>().getString(R.string.field_fiber), 0.0, 100.0).getErrorOrNull()
                } else null
            )
        }
    }

    fun onSodiumChange(sodium: String) {
        if (sodium.isEmpty() || sodium.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                sodium = sodium,
                sodiumError = if (_uiState.value.showValidationErrors) {
                    validateNutrition(sodium, getApplication<Application>().getString(R.string.field_sodium), 0, 10000).getErrorOrNull()
                } else null
            )
        }
    }

    /**
     * Validates nutrition field using Sealed Class pattern
     * @return ValidationResult.Valid if valid, ValidationResult.Invalid with error message if invalid
     */
    private fun validateNutrition(value: String, fieldName: String, min: Number, max: Number): ValidationResult {
        val context = getApplication<Application>()
        if (value.trim().isEmpty()) {
            return ValidationResult.Invalid(context.getString(R.string.error_nutrition_required, fieldName))
        }

        val numValue = value.toDoubleOrNull()
        return when {
            numValue == null -> ValidationResult.Invalid(context.getString(R.string.error_nutrition_invalid, fieldName))
            numValue < min.toDouble() || numValue > max.toDouble() -> {
                if (min is Int && max is Int) {
                    ValidationResult.Invalid(context.getString(R.string.error_nutrition_range_int, fieldName, min, max))
                } else {
                    ValidationResult.Invalid(context.getString(R.string.error_nutrition_range_double, fieldName, min.toDouble(), max.toDouble()))
                }
            }
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates image selection using Sealed Class pattern
     * @return ValidationResult.Valid if valid, ValidationResult.Invalid with error message if invalid
     */
    private fun validateImage(): ValidationResult {
        val context = getApplication<Application>()
        return if (_uiState.value.imageUri == null && _uiState.value.imageRes == null) {
            ValidationResult.Invalid(context.getString(R.string.error_image_required))
        } else {
            ValidationResult.Valid
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
    /**
     * Saves the recipe after validating all fields using Sealed Class pattern
     */
    fun saveRecipe(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(showValidationErrors = true)

        // Validate all fields using Sealed Class ValidationResult
        val nameValidation = validateRecipeName(_uiState.value.recipeName)
        val categoryValidation = validateCategory(_uiState.value.selectedCategory)
        val ingredientsValidation = validateIngredients(_uiState.value.ingredients)
        val instructionsValidation = validateInstructions(_uiState.value.instructions)
        val context = getApplication<Application>()
        val prepTimeValidation = validateTime(_uiState.value.prepTime, context.getString(R.string.field_prep_time))
        val cookTimeValidation = validateTime(_uiState.value.cookTime, context.getString(R.string.field_cook_time))
        val caloriesValidation = validateNutrition(_uiState.value.calories, context.getString(R.string.field_calories), 0, 10000)
        val fatValidation = validateNutrition(_uiState.value.fat, context.getString(R.string.field_fat), 0.0, 500.0)
        val proteinValidation = validateNutrition(_uiState.value.protein, context.getString(R.string.field_protein), 0.0, 500.0)
        val carbsValidation = validateNutrition(_uiState.value.carbs, context.getString(R.string.field_carbs), 0.0, 1000.0)
        val fiberValidation = validateNutrition(_uiState.value.fiber, context.getString(R.string.field_fiber), 0.0, 100.0)
        val sodiumValidation = validateNutrition(_uiState.value.sodium, context.getString(R.string.field_sodium), 0, 10000)
        val imageValidation = validateImage()

        // Update UI state with validation errors (convert ValidationResult to String?)
        _uiState.value = _uiState.value.copy(
            recipeNameError = nameValidation.getErrorOrNull(),
            categoryError = categoryValidation.getErrorOrNull(),
            ingredientsError = ingredientsValidation.getErrorOrNull(),
            instructionsError = instructionsValidation.getErrorOrNull(),
            prepTimeError = prepTimeValidation.getErrorOrNull(),
            cookTimeError = cookTimeValidation.getErrorOrNull(),
            caloriesError = caloriesValidation.getErrorOrNull(),
            fatError = fatValidation.getErrorOrNull(),
            proteinError = proteinValidation.getErrorOrNull(),
            carbsError = carbsValidation.getErrorOrNull(),
            fiberError = fiberValidation.getErrorOrNull(),
            sodiumError = sodiumValidation.getErrorOrNull(),
            imageError = imageValidation.getErrorOrNull()
        )

        // Check if any validation failed using Sealed Class pattern
        val validationResults = listOf(
            nameValidation, categoryValidation, ingredientsValidation, instructionsValidation,
            prepTimeValidation, cookTimeValidation, caloriesValidation, fatValidation,
            proteinValidation, carbsValidation, fiberValidation, sodiumValidation, imageValidation
        )

        val hasErrors = validationResults.any { it is ValidationResult.Invalid }

        if (hasErrors) {
            _uiState.value = _uiState.value.copy(
                error = context.getString(R.string.error_fix_errors_before_saving)
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