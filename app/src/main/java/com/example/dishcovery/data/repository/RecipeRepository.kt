package com.example.dishcovery.data.repository

import com.example.dishcovery.data.models.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class RecipeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    /**
     * Add a new recipe to Firebase
     */
    suspend fun addRecipe(recipe: Recipe): Result<String> {
        return try {
            val docRef = recipesCollection.add(recipe).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing recipe
     */
    suspend fun updateRecipe(recipeId: String, recipe: Recipe): Result<Unit> {
        return try {
            recipesCollection.document(recipeId)
                .set(recipe, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a single recipe by ID
     */
    suspend fun getRecipeById(recipeId: String): Result<Recipe?> {
        return try {
            val snapshot = recipesCollection.document(recipeId).get().await()
            val recipe = snapshot.toObject(Recipe::class.java)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all recipes
     */
    suspend fun getRecipes(): Result<List<Recipe>> {
        return try {
            val snapshot = recipesCollection.get().await()
            val recipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a recipe
     */
    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get recipes by category
     */
    suspend fun getRecipesByCategory(category: String): Result<List<Recipe>> {
        return try {
            val snapshot = recipesCollection
                .whereEqualTo("category", category)
                .get()
                .await()
            val recipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            recipesCollection.document(recipeId)
                .update("isFavorite", isFavorite)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}