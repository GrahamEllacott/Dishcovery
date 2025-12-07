package com.example.dishcovery.data.models

// Sample data classes - to be replaced with actual data models
data class ShoppingItem(
    val id: Int,
    val name: String,
    val quantity: String,
    var isChecked: Boolean = false,
    val recipeId: String? = null
)

data class ShoppingCategory(
    val id: Int,
    val name: String,
    val emoji: String,
    val items: MutableList<ShoppingItem>
)
