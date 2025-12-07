package com.example.dishcovery.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class CustomShoppingItem(
    val id: Int = 0,
    val name: String,
    val quantity: String = "",
    val isChecked: Boolean = false,
)

class ShoppingListDBHelper(context: Context) :
    SQLiteOpenHelper(context, "shopping_list.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE shopping_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                quantity TEXT,
                is_checked INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        // Add table for recipe ingredient checkboxes
        db?.execSQL(
            """
            CREATE TABLE recipe_ingredient_checks (
                recipe_id TEXT NOT NULL,
                ingredient TEXT NOT NULL,
                is_checked INTEGER DEFAULT 0,
                PRIMARY KEY (recipe_id, ingredient)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS shopping_items")
        db?.execSQL("DROP TABLE IF EXISTS recipe_ingredient_checks")
        onCreate(db)
    }

    // CREATE
    fun insertItem(item: CustomShoppingItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", item.name)
            put("quantity", item.quantity)
            put("is_checked", if (item.isChecked) 1 else 0)
        }
        db.insert("shopping_items", null, values)
        db.close()
    }

    // READ ALL
    fun getAllItems(): List<CustomShoppingItem> {
        val items = mutableListOf<CustomShoppingItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM shopping_items ORDER BY id DESC", null)

        while (cursor.moveToNext()) {
            items.add(
                CustomShoppingItem(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    quantity = cursor.getString(2) ?: "",
                    isChecked = cursor.getInt(3) == 1
                )
            )
        }

        cursor.close()
        db.close()
        return items
    }

    // READ BY ID
    fun getItemById(id: Int): CustomShoppingItem? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM shopping_items WHERE id = ?", arrayOf(id.toString()))

        var item: CustomShoppingItem? = null
        if (cursor.moveToFirst()) {
            item = CustomShoppingItem(
                id = cursor.getInt(0),
                name = cursor.getString(1),
                quantity = cursor.getString(2) ?: "",
                isChecked = cursor.getInt(3) == 1
            )
        }

        cursor.close()
        db.close()
        return item
    }

    // UPDATE
    fun updateItem(item: CustomShoppingItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", item.name)
            put("quantity", item.quantity)
            put("is_checked", if (item.isChecked) 1 else 0)
        }
        db.update("shopping_items", values, "id = ?", arrayOf(item.id.toString()))
        db.close()
    }

    // DELETE
    fun deleteItem(id: Int) {
        val db = writableDatabase
        db.delete("shopping_items", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    // DELETE ALL CHECKED
    fun deleteCheckedItems() {
        val db = writableDatabase
        db.delete("shopping_items", "is_checked = ?", arrayOf("1"))
        db.close()
    }

    // CLEAR ALL
    fun clearAllItems() {
        val db = writableDatabase
        db.delete("shopping_items", null, null)
        db.close()
    }

    // Recipe Ingredient Checkbox Methods
    fun setRecipeIngredientChecked(recipeId: String, ingredient: String, isChecked: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("recipe_id", recipeId)
            put("ingredient", ingredient)
            put("is_checked", if (isChecked) 1 else 0)
        }
        db.insertWithOnConflict("recipe_ingredient_checks", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getRecipeIngredientChecked(recipeId: String, ingredient: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT is_checked FROM recipe_ingredient_checks WHERE recipe_id = ? AND ingredient = ?",
            arrayOf(recipeId, ingredient)
        )

        var isChecked = false
        if (cursor.moveToFirst()) {
            isChecked = cursor.getInt(0) == 1
        }

        cursor.close()
        db.close()
        return isChecked
    }

    fun deleteCheckedRecipeIngredients() {
        val db = writableDatabase
        db.delete("recipe_ingredient_checks", "is_checked = ?", arrayOf("1"))
        db.close()
    }
}