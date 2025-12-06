package com.example.dishcovery.data.models

data class Recipe(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val imageURL: String = "",
    val prepTime: Int,
    val cookTime: Int = 0,
    val calories: Int,
    val category: String,
    var isFavorite: Boolean = false,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val fiber: Int = 0,
    val sodium: Int = 0
)
