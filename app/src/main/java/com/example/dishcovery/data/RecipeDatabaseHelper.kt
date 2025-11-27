package com.example.dishcovery.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dishcovery.data.models.Recipe

class RecipeDatabaseHelper(context : Context) :
    SQLiteOpenHelper(context, "recipes.db", null, 1) {

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(
                """
                    CREATE TABLE recipes (
                        id INTEGER PRIMARY KEY,
                        name TEXT,
                        imageRes INTEGER,
                        prepTime INTEGER,
                        calories INTEGER,
                        category TEXT,
                        isFavorite BOOLEAN
                    )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL("DROP TABLE IF EXISTS recipes")
            onCreate(db)
        }

    fun insertRecipe(recipe: Recipe) {
        val db = writableDatabase

        // create an array of column names and values
        val recipeValues = ContentValues().apply {
            put("name", recipe.name)
            put("imageRes", recipe.imageRes)
            put("prepTime", recipe.prepTime)
            put("calories", recipe.calories)
            put("category", recipe.category)
            put("isFavorite", recipe.isFavorite)
        }

        // insert the recipe into the database
        db.insert("recipes", null, recipeValues)
        // clean up
        db.close()

    }

    fun getAllRecipes(): List<Recipe> {
        // create a list to hold the recipes
        val recipes = mutableListOf<Recipe>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM recipes", null)
        // get all the rows
        while (cursor.moveToNext()) {
            recipes.add(
                Recipe(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    imageRes = cursor.getInt(2),
                    prepTime = cursor.getInt(3),
                    calories = cursor.getInt(4),
                    category = cursor.getString(5),
                    isFavorite = cursor.getInt(6) == 1
                )
            )
        }
        // clean up
        cursor.close()
        db.close()
        // return the list of recipes
        return recipes
    }

    fun updateRecipe(recipe: Recipe) {
        val db = writableDatabase
        // create an array of column names and values
        val recipeValues = ContentValues().apply {
            put("name", recipe.name)
            put("imageRes", recipe.imageRes)
            put("prepTime", recipe.prepTime)
            put("calories", recipe.calories)
            put("category", recipe.category)
            put("isFavorite", recipe.isFavorite)
        }
        // update the recipe in the database
        db.update("recipes", recipeValues, "id = ?", arrayOf(recipe.id.toString()))
        // clean up
        db.close()
    }

    fun deleteRecipe(recipe: Recipe) {
        val db = writableDatabase
        // delete the recipe from the database
        db.delete("recipes", "id = ?", arrayOf(recipe.id.toString()))
        db.close()
    }
}