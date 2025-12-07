package com.example.dishcovery.features.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.CustomShoppingItem
import com.example.dishcovery.data.ShoppingListDBHelper
import com.example.dishcovery.data.models.ShoppingCategory
import com.example.dishcovery.data.models.ShoppingItem
import com.example.dishcovery.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val categories: List<ShoppingCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hideChecked: Boolean = false
)

class ShoppingListViewModel : ViewModel() {

    private lateinit var dbHelper: ShoppingListDBHelper
    private lateinit var recipeRepository: RecipeRepository

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun initRepository(context: Context) {
        dbHelper = ShoppingListDBHelper(context)
        recipeRepository = RecipeRepository(context)
        loadShoppingList()
    }

    fun loadShoppingList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val categories = mutableListOf<ShoppingCategory>()

            // Load recipes from Firebase
            recipeRepository.getRecipes()
                .onSuccess { recipes ->
                    if (recipes.isNotEmpty()) {
                        // Create "My Recipes" category with all recipe ingredients
                        val recipeItems = mutableListOf<ShoppingItem>()
                        var itemId = 1

                        recipes.forEach { recipe ->
                            recipe.ingredients.forEachIndexed { index, ingredient ->
                                // Get checked state from DB
                                val isChecked = dbHelper.getRecipeIngredientChecked(recipe.id, ingredient)

                                recipeItems.add(
                                    ShoppingItem(
                                        id = itemId++,
                                        name = ingredient,
                                        quantity = "from ${recipe.name}",
                                        isChecked = isChecked,
                                        recipeId = recipe.id
                                    )
                                )
                            }
                        }

                        // Filter out checked items if hideChecked is true
                        val filteredRecipeItems = if (_uiState.value.hideChecked) {
                            recipeItems.filter { !it.isChecked }.toMutableList()
                        } else {
                            recipeItems
                        }

                        categories.add(
                            ShoppingCategory(
                                id = 1,
                                name = "My Recipes Ingredients",
                                emoji = "🍳",
                                items = filteredRecipeItems
                            )
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load recipes: ${e.message}"
                    )
                }

            // Load custom items from SQLite
            val customItems = dbHelper.getAllItems()
            if (customItems.isNotEmpty()) {
                val customShoppingItems = customItems.map { item ->
                    ShoppingItem(
                        id = item.id,
                        name = item.name,
                        quantity = item.quantity,
                        isChecked = item.isChecked
                    )
                }

                // Filter out checked items if hideChecked is true
                val filteredCustomItems = if (_uiState.value.hideChecked) {
                    customShoppingItems.filter { !it.isChecked }.toMutableList()
                } else {
                    customShoppingItems.toMutableList()
                }

                categories.add(
                    ShoppingCategory(
                        id = 2,
                        name = "Custom Ingredients",
                        emoji = "🥗",
                        items = filteredCustomItems
                    )
                )
            }

            _uiState.value = _uiState.value.copy(
                categories = categories,
                isLoading = false
            )
        }
    }

    fun addCustomItem(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                val item = CustomShoppingItem(
                    name = name.trim(),
                    quantity = "",
                    isChecked = false
                )
                dbHelper.insertItem(item)
                loadShoppingList() // Reload
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add item: ${e.message}"
                )
            }
        }
    }

    fun toggleItem(categoryId: Int, itemId: Int, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                if (categoryId == 1) {
                    // Recipe ingredient - save to recipe_ingredient_checks table
                    val category = _uiState.value.categories.find { it.id == 1 }
                    val item = category?.items?.find { it.id == itemId }

                    item?.let {
                        dbHelper.setRecipeIngredientChecked(
                            it.recipeId ?: "",
                            it.name,
                            isChecked
                        )
                    }
                } else if (categoryId == 2) {
                    // Custom item - update in shopping_items table
                    val item = dbHelper.getItemById(itemId)
                    item?.let {
                        val updatedItem = it.copy(isChecked = isChecked)
                        dbHelper.updateItem(updatedItem)
                    }
                }

                // Update UI state
                val updatedCategories = _uiState.value.categories.map { category ->
                    if (category.id == categoryId) {
                        val updatedItems = category.items.map { item ->
                            if (item.id == itemId) {
                                item.copy(isChecked = isChecked)
                            } else {
                                item
                            }
                        }.toMutableList()
                        category.copy(items = updatedItems)
                    } else {
                        category
                    }
                }

                _uiState.value = _uiState.value.copy(categories = updatedCategories)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update item: ${e.message}"
                )
            }
        }
    }

    fun toggleHideChecked() {
        _uiState.value = _uiState.value.copy(hideChecked = !_uiState.value.hideChecked)
        loadShoppingList() // Reload to apply filter
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}