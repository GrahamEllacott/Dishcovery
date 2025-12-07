package com.example.dishcovery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.dishcovery.data.api.ImgbbApiService
import com.example.dishcovery.data.models.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class RecipeRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val imgbbApi = ImgbbApiService.create()

    /**
     * Upload image to imgbb and return the permanent URL
     */
    private suspend fun uploadImageToImgbb(imageUri: String): Result<String> {
        return try {
            val uri = Uri.parse(imageUri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                return Result.failure(Exception("Failed to load image"))
            }

            // Compress and resize bitmap
            val outputStream = ByteArrayOutputStream()
            val maxWidth = 1200
            val scaledBitmap = if (bitmap.width > maxWidth) {
                val ratio = maxWidth.toFloat() / bitmap.width
                val newHeight = (bitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
            } else {
                bitmap
            }

            // Compress to JPEG with quality 85
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()
            bitmap.recycle()
            if (bitmap != scaledBitmap) scaledBitmap.recycle()

            // Convert to Base64 (without prefix)
            val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // Upload to imgbb
            val response = imgbbApi.uploadImage(
                apiKey = ImgbbApiService.getApiKey(),
                imageBase64 = base64Image
            )

            if (response.success && response.data?.display_url != null) {
                Result.success(response.data.display_url)
            } else {
                Result.failure(Exception("Failed to upload image: Status ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Upload error: ${e.message}", e))
        }
    }

    /**
     * Add a new recipe to Firebase with imgbb image upload
     */
    suspend fun addRecipe(recipe: Recipe): Result<String> {
        return try {
            // Upload image to imgbb if there's a local URI
            val imageUrl = if (!recipe.imageUri.isNullOrEmpty() &&
                !recipe.imageUri.startsWith("http")) {
                uploadImageToImgbb(recipe.imageUri).getOrThrow()
            } else {
                recipe.imageUri
            }

            // Create recipe with imgbb URL
            val recipeToSave = recipe.copy(imageUri = imageUrl)
            val docRef = recipesCollection.add(recipeToSave).await()

            // Update the recipe with its own ID
            recipesCollection.document(docRef.id)
                .update("id", docRef.id)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing recipe with imgbb image handling
     */
    suspend fun updateRecipe(recipeId: String, recipe: Recipe): Result<Unit> {
        return try {
            // Upload new image to imgbb if there's a local URI
            val imageUrl = if (!recipe.imageUri.isNullOrEmpty() &&
                !recipe.imageUri.startsWith("http")) {
                uploadImageToImgbb(recipe.imageUri).getOrThrow()
            } else {
                recipe.imageUri
            }

            // Update recipe with imgbb URL
            val recipeToSave = recipe.copy(id = recipeId, imageUri = imageUrl)
            recipesCollection.document(recipeId)
                .set(recipeToSave, SetOptions.merge())
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
            val recipe = snapshot.toObject(Recipe::class.java)?.copy(id = recipeId)
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
            val recipes = snapshot.documents.mapNotNull {
                it.toObject(Recipe::class.java)?.copy(id = it.id)
            }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a recipe (Note: imgbb images stay uploaded, but that's fine for free tier)
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
            val recipes = snapshot.documents.mapNotNull {
                it.toObject(Recipe::class.java)?.copy(id = it.id)
            }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Toggle ingredient checkbox
     */
    suspend fun toggleIngredient(recipeId: String, ingredientIndex: Int, isChecked: Boolean): Result<Unit> {
        return try {
            val recipe = getRecipeById(recipeId).getOrNull()
            if (recipe != null) {
                val updatedCheckedIngredients = recipe.checkedIngredients.toMutableList()

                // Ensure list is the right size
                while (updatedCheckedIngredients.size < recipe.ingredients.size) {
                    updatedCheckedIngredients.add(false)
                }

                if (ingredientIndex in updatedCheckedIngredients.indices) {
                    updatedCheckedIngredients[ingredientIndex] = isChecked
                }

                recipesCollection.document(recipeId)
                    .update("checkedIngredients", updatedCheckedIngredients)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle instruction checkbox
     */
    suspend fun toggleInstruction(recipeId: String, instructionIndex: Int, isChecked: Boolean): Result<Unit> {
        return try {
            val recipe = getRecipeById(recipeId).getOrNull()
            if (recipe != null) {
                val updatedCheckedInstructions = recipe.checkedInstructions.toMutableList()

                // Ensure list is the right size
                while (updatedCheckedInstructions.size < recipe.instructions.size) {
                    updatedCheckedInstructions.add(false)
                }

                if (instructionIndex in updatedCheckedInstructions.indices) {
                    updatedCheckedInstructions[instructionIndex] = isChecked
                }

                recipesCollection.document(recipeId)
                    .update("checkedInstructions", updatedCheckedInstructions)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}