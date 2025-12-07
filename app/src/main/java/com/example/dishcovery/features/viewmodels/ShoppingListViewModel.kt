package com.example.dishcovery.features.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.CustomShoppingItem
import com.example.dishcovery.data.ShoppingListDBHelper
import com.example.dishcovery.data.models.Recipe
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
    val error: String? = null
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
                            recipe.ingredients.forEach { ingredient ->
                                recipeItems.add(
                                    ShoppingItem(
                                        id = itemId++,
                                        name = ingredient,
                                        quantity = "from ${recipe.name}",
                                        isChecked = false
                                    )
                                )
                            }
                        }

                        categories.add(
                            ShoppingCategory(
                                id = 1,
                                name = "My Recipes Ingredients",
                                emoji = "🍳",
                                items = recipeItems
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
                }.toMutableList()

                categories.add(
                    ShoppingCategory(
                        id = 2,
                        name = "Custom Ingredients",
                        emoji = "🥗",
                        items = customShoppingItems
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
                // Only update custom items in DB (categoryId = 2)
                if (categoryId == 2) {
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

    fun clearAllChecked() {
        viewModelScope.launch {
            try {
                // Delete checked custom items from DB
                dbHelper.deleteCheckedItems()

                // Remove checked items from UI
                val updatedCategories = _uiState.value.categories.map { category ->
                    val filteredItems = category.items.filter { !it.isChecked }.toMutableList()
                    category.copy(items = filteredItems)
                }

                _uiState.value = _uiState.value.copy(categories = updatedCategories)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to clear items: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}