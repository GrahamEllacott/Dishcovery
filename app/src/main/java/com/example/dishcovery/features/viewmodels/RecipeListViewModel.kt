package com.example.dishcovery.features.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.BuildConfig
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.remote.RetrofitInstance
import com.example.dishcovery.data.repository.RecipeRepository
import com.example.dishcovery.util.RecipeCache
import com.example.dishcovery.util.RecipeParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipeListViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: RecipeRepository = RecipeRepository(application)
    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadMyRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            var sampleRecipes = emptyList<Recipe>()
            try {
                sampleRecipes = repository.getRecipes().getOrNull() ?: emptyList()

                // Apply category filter if not "All" or "My Recipes"
                if (_uiState.value.selectedCategory !in listOf("All", "My Recipes")) {
                    val categoryFilter = _uiState.value.selectedCategory.lowercase()
                    sampleRecipes = sampleRecipes.filter {
                        it.category.lowercase() == categoryFilter
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeListViewModel", "Error loading recipes: $e", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            _uiState.value = _uiState.value.copy(
                recipes = sampleRecipes,
                isLoading = false
            )
        }
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            var sampleRecipes = emptyList<Recipe>()

            try {
                // Determine the dish type for API filtering
                val dishType = when (_uiState.value.selectedCategory.lowercase()) {
                    "breakfast" -> "breakfast"
                    "lunch" -> "lunch"
                    "dinner" -> "main course"
                    "snacks" -> "snack"
                    else -> null // No filter for "All"
                }

                // GET RANDOM RESULTS
                if (_uiState.value.searchQuery.isEmpty()) {
                    // Get a list of random recipes and parse them into our recipe format
                    val response = RetrofitInstance.api.getRandomRecipes(
                        apiKey = BuildConfig.SPOON_API_KEY,
                        number = 10,
                        includeNutrition = true,
                        tags = dishType
                    )

                    Log.d("RecipeListViewModel", "${response.recipes.count()} Recipes Loaded")

                    val apiRecipes = response.recipes.mapIndexed { index, apiRecipe ->
                        val recipe = RecipeParser.parse(apiRecipe)
                        // Assign temp ID
                        val recipeWithId = recipe.copy(id = "temp_api_${System.currentTimeMillis()}_$index")
                        // Cache the recipe
                        RecipeCache.put(recipeWithId.id, recipeWithId)
                        Log.d("RecipeListViewModel", "Cached recipe ${recipeWithId.id}: ${recipeWithId.name}")
                        recipeWithId
                    }

                    sampleRecipes += apiRecipes

                    Log.d("RecipeListViewModel", "${sampleRecipes.count()} Recipes Parsed")

                } else {
                    // SEARCH FOR RESULTS

                    // Get all recipes from firebase and filter
                    var firebaseRecipes = repository.getRecipes().getOrNull() ?: emptyList()

                    // Filter by search query
                    firebaseRecipes = firebaseRecipes.filter {
                        it.name.contains(_uiState.value.searchQuery, ignoreCase = true)
                    }

                    // Filter by category if applicable
                    if (dishType != null) {
                        val categoryFilter = _uiState.value.selectedCategory.lowercase()
                        firebaseRecipes = firebaseRecipes.filter {
                            it.category.lowercase() == categoryFilter
                        }
                    }

                    sampleRecipes += firebaseRecipes

                    // Get up to 10 results from spoonacular API
                    Log.d("RecipeListViewModel", "Searching for Recipes")

                    // Use a search query to get a list of recipes with optional type filter
                    val response = RetrofitInstance.api.getSearchRecipes(
                        apiKey = BuildConfig.SPOON_API_KEY,
                        query = _uiState.value.searchQuery,
                        number = 10,
                        type = dishType
                    )

                    Log.d("RecipeListViewModel", "${response.results.count()} Recipes Loaded")

                    // Get IDs for second request
                    val ids = response.results.joinToString(",") { it.id.toString() }

                    // If no recipes are found then show an error message
                    if (ids.isEmpty()) {
                        if (firebaseRecipes.isEmpty()) {
                            _uiState.value = _uiState.value.copy(error = "No recipes found")
                        }
                    } else {
                        Log.d("RecipeListViewModel", "Loading recipe details")
                        // Get the details from the recipe stubs and parse them into our recipe format
                        val recipeInfo = RetrofitInstance.api.getRecipeInformation(
                            apiKey = BuildConfig.SPOON_API_KEY,
                            ids = ids,
                            includeNutrition = true
                        )

                        val apiRecipes = recipeInfo.mapIndexed { index, apiRecipe ->
                            val recipe = RecipeParser.parse(apiRecipe)
                            // Assign temp ID
                            val recipeWithId = recipe.copy(id = "temp_search_${System.currentTimeMillis()}_$index")
                            // Cache the recipe
                            RecipeCache.put(recipeWithId.id, recipeWithId)
                            Log.d("RecipeListViewModel", "Cached recipe ${recipeWithId.id}: ${recipeWithId.name}")
                            recipeWithId
                        }

                        sampleRecipes += apiRecipes

                        Log.d("RecipeListViewModel", "${sampleRecipes.count()} Recipes Parsed")
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeListViewModel", "Error loading recipes: $e", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }

            _uiState.value = _uiState.value.copy(
                recipes = sampleRecipes,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onSearchQuerySubmit() {
        loadRecipes()
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        if (category == "My Recipes") {
            loadMyRecipes()
        } else {
            loadRecipes()
        }
    }
}