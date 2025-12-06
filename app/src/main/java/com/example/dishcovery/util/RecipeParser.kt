package com.example.dishcovery.util

import com.example.dishcovery.R
import com.example.dishcovery.data.models.ApiRecipe
import com.example.dishcovery.data.models.Recipe

class RecipeParser {
    companion object {
        fun parse(apiRecipe: ApiRecipe): Recipe {

            // take the ApiRecipe object we get from our API calls and convert it to our simpler recipe format

            val id: Int = (apiRecipe.id ?: 0).toInt()
            val name: String = apiRecipe.title ?: ""
            val imageRes: Int = R.drawable.ic_launcher_background
            val imageURL: String = apiRecipe.image ?: ""
            val prepTime: Int = apiRecipe.readyInMinutes?.toInt() ?: 0
            val cookTime: Int = apiRecipe.cookingMinutes?.toInt() ?: 0
            val category: String = ""

            // all loaded recipes are not favorited
            // this will be changed by the user later
            var isFavorite: Boolean = false

            val ingredients: List<String> = apiRecipe.extendedIngredients?.map { it.name ?: "" } ?: emptyList()
            val instructions: List<String> = apiRecipe.analyzedInstructions?.firstOrNull()?.steps?.map { it.step ?: "" } ?: emptyList()

            var calories: Int = 0
            var fat: Int = 0
            var carbs: Int = 0
            var sodium: Int = 0
            var protein: Int = 0

            // if the nutrients info exists then parse it, otherwise 0
            if(apiRecipe.nutrition != null && apiRecipe.nutrition.nutrients != null){
                calories = apiRecipe.nutrition.nutrients[0].amount?.toInt() ?: 0
                fat = apiRecipe.nutrition.nutrients[1].amount?.toInt() ?: 0
                carbs = apiRecipe.nutrition.nutrients[3].amount?.toInt() ?: 0
                sodium = apiRecipe.nutrition.nutrients[7].amount?.toInt() ?: 0
                protein = apiRecipe.nutrition.nutrients[8].amount?.toInt() ?: 0
            }

            return Recipe(
                id = id,
                name = name,
                imageRes = imageRes,
                imageURL = imageURL,
                prepTime = prepTime,
                cookTime = cookTime,
                calories = calories,
                fat = fat,
                carbs = carbs,
                sodium = sodium,
                protein = protein,
                category = category,
                isFavorite = isFavorite,
                ingredients = ingredients,
                instructions = instructions
            )
        }
    }
}