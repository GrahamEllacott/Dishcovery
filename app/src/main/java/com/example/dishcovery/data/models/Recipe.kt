package com.example.dishcovery.data.models

// To update this class when work on Recipe Detail Screen (add other fields)
data class Recipe(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val prepTime: Int,
    val calories: Int,
    val category: String,
    var isFavorite: Boolean = false
)