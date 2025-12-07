package com.example.dishcovery.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dishcovery.data.models.MealPlan
import java.time.LocalDate

class MealPlanDBHelper(context: Context) :
    SQLiteOpenHelper(context, "mealplans.db", null, 1)
    {
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(
                """
                CREATE TABLE mealplans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date DATE NOT NULL,
                    recipe_ids TEXT NOT NULL
                )
            """.trimIndent()
            )
        }

        override fun onUpgrade(
            db: SQLiteDatabase?,
            oldVersion: Int,
            newVersion: Int
        ) {
            db?.execSQL("DROP TABLE IF EXISTS mealplans")
            onCreate(db)
        }

        //CREATE
        fun insertMealPlan(mealPlan: MealPlan) {
            val db = writableDatabase
            // create a map of column names to values
            val mealPlanValues = ContentValues().apply {
                put("date", mealPlan.date.toString())
                put("recipe_ids", mealPlan.recipeIds.joinToString(","))
            }

            db.insert("mealplans", null, mealPlanValues)
            db.close()
        }

        fun getMealPlanByDate(date: String): MealPlan? {
            val mealPlans = mutableListOf<MealPlan>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * from mealplans WHERE date = ?", arrayOf(date))
            while (cursor.moveToNext()) {
                mealPlans.add(
                    MealPlan(
                        id = cursor.getInt(0),
                        date = LocalDate.parse(cursor.getString(1)),
                        recipeIds = cursor.getString(2).split(","),
                        recipes = emptyList()
                    )
                )
            }
            // clean up our mess
            cursor.close()
            db.close()
            // return all the rows we found
            return mealPlans.firstOrNull()
        }

        // UPDATE
        fun updateMealPlan(mealPlan: MealPlan) {
            val db = writableDatabase
            val mealPlanValues = ContentValues().apply {
                put("date", mealPlan.date.toString())
                put("recipe_ids", mealPlan.recipeIds.joinToString(","))
            }
            db.update("mealplans", mealPlanValues, "id = ?", arrayOf(mealPlan.id.toString()))
            db.close()
        }

        // DELETE
        fun deleteMealPlan(id: Int) {
            val db = writableDatabase
            db.delete("mealplans", "id = ?", arrayOf(id.toString()))
            db.close()
        }
    }
