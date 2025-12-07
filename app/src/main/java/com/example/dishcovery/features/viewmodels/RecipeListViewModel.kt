package com.example.dishcovery.features.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.BuildConfig
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.remote.RetrofitInstance
import com.example.dishcovery.data.repository.RecipeRepository
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

    // Add this to cache the current recipes
    private val recipesCache = mutableMapOf<String, Recipe>()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            var sampleRecipes = emptyList<Recipe>()

            try{
                // GET RANDOM RESULTS
                if(_uiState.value.searchQuery.isEmpty()){
                    sampleRecipes = repository.getRecipes().getOrNull() ?: emptyList()
                    Log.d("RecipeListViewModel", "Loading random recipes")

                    val response = RetrofitInstance.api.getRandomRecipes(BuildConfig.SPOON_API_KEY, 10)
                    Log.d("RecipeListViewModel", "${response.recipes.count()} Recipes Loaded")

                    sampleRecipes += response.recipes.map { apiRecipe -> RecipeParser.parse(apiRecipe) }
                    Log.d("RecipeListViewModel", "${sampleRecipes.count()} Recipes Parsed")

                }else{
                    sampleRecipes = repository.getRecipes().getOrNull() ?: emptyList()
                    sampleRecipes = sampleRecipes.filter { it.name.contains(_uiState.value.searchQuery, ignoreCase = true) }

                    Log.d("RecipeListViewModel", "Searching for Recipes")
                    val response = RetrofitInstance.api.getSearchRecipes(BuildConfig.SPOON_API_KEY, _uiState.value.searchQuery, 10)
                    Log.d("RecipeListViewModel", "${response.results.count()} Recipes Loaded")

                    val ids = response.results.joinToString(",") { it.id.toString() }

                    if (ids.isEmpty()) {
                        _uiState.value = _uiState.value.copy(error = "No recipes found, \nTry searching something else")
                    }else{
                        Log.d("RecipeListViewModel", "Loading recipe details")
                        val recipeInfo = RetrofitInstance.api.getRecipeInformation(BuildConfig.SPOON_API_KEY, ids)
                        sampleRecipes += recipeInfo.map { apiRecipe -> RecipeParser.parse(apiRecipe) }
                        Log.d("RecipeListViewModel", "${sampleRecipes.count()} Recipes Parsed")
                    }
                }

                // Cache all recipes with a temporary ID if they don't have one
                sampleRecipes.forEachIndexed { index, recipe ->
                    val recipeId = recipe.id.ifEmpty { "temp_${System.currentTimeMillis()}_$index" }
                    recipesCache[recipeId] = recipe.copy(id = recipeId)
                }

            }catch(e: Exception){
                Log.e("RecipeListViewModel", "Error loading recipes: $e", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }

            _uiState.value = _uiState.value.copy(
                recipes = sampleRecipes.map { recipe ->
                    if (recipe.id.isEmpty()) {
                        val tempId = "temp_${System.currentTimeMillis()}_${sampleRecipes.indexOf(recipe)}"
                        recipe.copy(id = tempId)
                    } else {
                        recipe
                    }
                },
                isLoading = false
            )
        }
    }

    // Add method to get cached recipe
    fun getCachedRecipe(recipeId: String): Recipe? {
        return recipesCache[recipeId]
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onSearchQuerySubmit() {
        loadRecipes()
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadRecipes()
    }
}