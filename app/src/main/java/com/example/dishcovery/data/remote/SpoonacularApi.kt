package com.example.dishcovery.data.remote

import com.example.dishcovery.data.models.ApiRecipe
import com.example.dishcovery.data.models.RandomResult
import com.example.dishcovery.data.models.SearchResult
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApi {
    @GET("recipes/random")
    suspend fun getRandomRecipes(@Query("apiKey") apiKey: String, @Query("number") number: Int, @Query("includeNutrition") includeNutrition: Boolean = true): RandomResult

    // this only returns recipe stubs, a second api call is needed to get the details
    @GET("recipes/complexSearch")
    suspend fun getSearchRecipes(@Query("apiKey") apiKey: String, @Query("query") query: String ,@Query("number") number: Int): SearchResult

    // this is to get all the recipe details from the recipe stubs in the search
    @GET("recipes/informationBulk")
    suspend fun getRecipeInformation(@Query("apiKey") apiKey: String, @Query("ids") ids: String, @Query("includeNutrition") includeNutrition: Boolean = true): ArrayList<ApiRecipe>
}
