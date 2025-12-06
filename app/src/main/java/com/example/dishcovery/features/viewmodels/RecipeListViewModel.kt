package com.example.dishcovery.features.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.data.remote.RetrofitInstance
import com.example.dishcovery.util.RecipeParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.dishcovery.BuildConfig

data class RecipeListUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipeListViewModel : ViewModel() {
    var recipes by mutableStateOf(emptyList<Recipe>())
        private set
    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

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

                    Log.d("RecipeListViewModel", "Loading random recipes")

                    // get a list of random recipes and parse them into our recipe format
                    val response = RetrofitInstance.api.getRandomRecipes(BuildConfig.SPOON_API_KEY, 10)

                    Log.d("RecipeListViewModel", "${response.recipes.count()} Recipes Loaded")

                    sampleRecipes = response.recipes.map { apiRecipe -> RecipeParser.parse(apiRecipe) }

                    Log.d("RecipeListViewModel", "${sampleRecipes.count()} Recipes Parsed")

                }else{
                    //SEARCH FOR RESULTS
                    //TODO: Implement category filters

                    Log.d("RecipeListViewModel", "Searching for Recipes")
                    //use a search query to get a list of recipes
                    val response = RetrofitInstance.api.getSearchRecipes(BuildConfig.SPOON_API_KEY, _uiState.value.searchQuery, 10)

                    Log.d("RecipeListViewModel", "${response.results.count()} Recipes Loaded")

                    // get IDs for second request
                    val ids = response.results.joinToString(",") { it.id.toString() }

                    // if no recipes are found then show an error message
                    if (ids.isEmpty()) {
                        _uiState.value = _uiState.value.copy(error = "No recipes found")
                    }else{
                        Log.d("RecipeListViewModel", "Loading recipe details")
                        // get the details from the recipe stubs and parse them into our recipe format
                        val recipeInfo = RetrofitInstance.api.getRecipeInformation(BuildConfig.SPOON_API_KEY, ids)

                        sampleRecipes = recipeInfo.map { apiRecipe -> RecipeParser.parse(apiRecipe) }

                        Log.d("RecipeListViewModel", "${sampleRecipes.count()} Recipes Parsed")
                    }
                }
            }catch(e: Exception){
                Log.e("RecipeListViewModel", "Error loading recipes: $e", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
            recipes = sampleRecipes
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
        loadRecipes()
    }

    fun toggleFavorite(recipeId: Int) {
        val updatedRecipes = recipes.map { recipe ->
            if (recipe.id == recipeId) {
                recipe.copy(isFavorite = !recipe.isFavorite)
            } else {
                recipe
            }
        }
        recipes = updatedRecipes
    }

}