package com.example.dishcovery.data.remote

import com.example.dishcovery.data.models.ApiRecipe
import com.example.dishcovery.data.models.RandomResult
import com.example.dishcovery.data.models.SearchResult
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApi {
    /**
     * Get random recipes
     * @param apiKey Your Spoonacular API key
     * @param number Number of recipes to return
     * @param includeNutrition Include nutrition information (default: true)
     */
    @GET("recipes/random")
    suspend fun getRandomRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int,
        @Query("includeNutrition") includeNutrition: Boolean = true
    ): RandomResult

    /**
     * Get random recipes filtered by dish type
     * @param apiKey Your Spoonacular API key
     * @param number Number of recipes to return
     * @param includeNutrition Include nutrition information (default: true)
     * @param tags Dish type filter (e.g., "breakfast", "lunch", "main course", "snack")
     */
    @GET("recipes/random")
    suspend fun getRandomRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int,
        @Query("includeNutrition") includeNutrition: Boolean = true,
        @Query("tags") tags: String?
    ): RandomResult

    /**
     * Search for recipes by query
     * This only returns recipe stubs, a second api call is needed to get the details
     * @param apiKey Your Spoonacular API key
     * @param query Search query (recipe name, ingredient, etc.)
     * @param number Number of results to return
     */
    @GET("recipes/complexSearch")
    suspend fun getSearchRecipes(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String,
        @Query("number") number: Int
    ): SearchResult

    /**
     * Search for recipes by query with dish type filter
     * This only returns recipe stubs, a second api call is needed to get the details
     * @param apiKey Your Spoonacular API key
     * @param query Search query (recipe name, ingredient, etc.)
     * @param number Number of results to return
     * @param type Dish type filter (e.g., "breakfast", "lunch", "main course", "snack")
     */
    @GET("recipes/complexSearch")
    suspend fun getSearchRecipes(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String,
        @Query("number") number: Int,
        @Query("type") type: String?
    ): SearchResult

    /**
     * Get all the recipe details from the recipe stubs in the search
     * @param apiKey Your Spoonacular API key
     * @param ids Comma-separated list of recipe IDs
     * @param includeNutrition Include nutrition information (default: true)
     */
    @GET("recipes/informationBulk")
    suspend fun getRecipeInformation(
        @Query("apiKey") apiKey: String,
        @Query("ids") ids: String,
        @Query("includeNutrition") includeNutrition: Boolean = true
    ): ArrayList<ApiRecipe>
}