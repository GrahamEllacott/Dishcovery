package com.example.dishcovery.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: String = "",
    val name: String = "",
    val imageRes: Int = 0,
    val imageUri: String? = null,
    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val calories: Int = 0,
    val category: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val fiber: Int = 0,
    val sodium: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val checkedIngredients: List<Boolean> = emptyList(),
    val checkedInstructions: List<Boolean> = emptyList(),
) : Parcelable {
    // No-arg constructor for Firebase
    constructor() : this(
        id = "",
        name = "",
        imageRes = 0,
        imageUri = null,
        prepTime = 0,
        cookTime = 0,
        calories = 0,
        category = "",
        ingredients = emptyList(),
        instructions = emptyList(),
        protein = 0,
        carbs = 0,
        fat = 0,
        fiber = 0,
        sodium = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        checkedIngredients = emptyList(),
        checkedInstructions = emptyList()
    )
}