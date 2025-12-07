package com.example.dishcovery.util

import com.example.dishcovery.data.models.Recipe

object RecipeCache {
    private val cache = mutableMapOf<String, Recipe>()

    fun put(recipeId: String, recipe: Recipe) {
        cache[recipeId] = recipe
    }

    fun get(recipeId: String): Recipe? {
        return cache[recipeId]
    }

    fun clear(recipeId: String) {
        cache.remove(recipeId)
    }

    fun clearAll() {
        cache.clear()
    }
}