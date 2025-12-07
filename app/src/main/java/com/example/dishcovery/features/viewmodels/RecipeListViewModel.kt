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
            _uiState.value = _uiState.value.copy(isLoading = true)
            var sampleRecipes = emptyList<Recipe>()
            try {
                sampleRecipes = repository.getRecipes().getOrNull() ?: emptyList()
            }
            catch (e: Exception) {

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
            _uiState.value = _uiState.value.copy(isLoading = true)
            var sampleRecipes = emptyList<Recipe>()

            try{
                // GET RANDOM RESULTS
                if(_uiState.value.searchQuery.isEmpty()){

                    // get a list of random recipes and parse them into our recipe format
                    val response = RetrofitInstance.api.getRandomRecipes(BuildConfig.SPOON_API_KEY, 10)

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

                }else{
                    //SEARCH FOR RESULTS
                    //TODO: Implement category filters

                    //get all recipes from firebase
                    sampleRecipes = repository.getRecipes().getOrNull() ?: emptyList()
                    //filter by title
                    sampleRecipes = sampleRecipes.filter { it.name.contains(_uiState.value.searchQuery, ignoreCase = true) }


                    //get up to 10 results from spoonacular API
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
            }catch(e: Exception){
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
        if (category == "All") {
            loadRecipes()
        }
        else if (category == "My Recipes") {
            loadMyRecipes()
        }


    }
}