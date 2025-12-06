package com.example.dishcovery.features.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
    val filteredRecipes: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipeListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // TODO: Replace with actual repository call
            // For now, using sample data
            val sampleRecipes = getSampleRecipes()
            _uiState.value = _uiState.value.copy(
                recipes = sampleRecipes,
                filteredRecipes = sampleRecipes,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterRecipes()
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterRecipes()
    }

    fun toggleFavorite(recipeId: String) {
        val updatedRecipes = _uiState.value.recipes.map { recipe ->
            if (recipe.id == recipeId) {
                recipe.copy(isFavorite = !recipe.isFavorite)
            } else {
                recipe
            }
        }
        _uiState.value = _uiState.value.copy(recipes = updatedRecipes)
        filterRecipes()
    }

    private fun filterRecipes() {
        val filtered = _uiState.value.recipes.filter { recipe ->
            val matchesSearch = recipe.name.contains(_uiState.value.searchQuery, ignoreCase = true)
            val matchesCategory = _uiState.value.selectedCategory == "All" ||
                    recipe.category == _uiState.value.selectedCategory
            matchesSearch && matchesCategory
        }
        _uiState.value = _uiState.value.copy(filteredRecipes = filtered)
    }

    private fun getSampleRecipes(): List<Recipe> {
        // TODO: Remove when API is connected
        return listOf(
            Recipe(
                id = "id1",
                name = "Spaghetti Carbonara",
                imageRes = com.example.dishcovery.R.drawable.ic_launcher_background,
                prepTime = 15,
                cookTime = 10,
                calories = 520,
                category = "Dinner",
                ingredients = listOf("400g spaghetti", "200g pancetta", "4 eggs"),
                instructions = listOf("Boil water", "Cook pasta", "Mix ingredients")
            ),
            Recipe(
                id = "id2",
                name = "Honey Garlic Chicken",
                imageRes = com.example.dishcovery.R.drawable.ic_launcher_background,
                prepTime = 20,
                cookTime = 15,
                calories = 380,
                category = "Dinner",
                ingredients = listOf("500g chicken", "3 tbsp honey", "4 cloves garlic"),
                instructions = listOf("Marinate chicken", "Cook in pan")
            )
        )
    }
}