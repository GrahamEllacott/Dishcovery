package com.example.dishcovery.features.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dishcovery.components.mealPlan.DayMealPlanCard
import com.example.dishcovery.components.mealPlan.RecipeSelectionDialog
import com.example.dishcovery.components.mealPlan.WeekSelector
import com.example.dishcovery.features.viewmodels.WeeklyMealPlanViewModel
import java.time.LocalDate
import androidx.compose.ui.res.stringResource
import com.example.dishcovery.R

@Composable
fun WeeklyMealPlanScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: WeeklyMealPlanViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Show recipe selection dialog when needed
    if (uiState.showRecipeDialog) {
        RecipeSelectionDialog(
            recipes = uiState.allRecipes,
            isLoading = uiState.isLoadingRecipes,
            onRecipeSelected = { recipe ->
                viewModel.addRecipeToMealPlan(recipe)
            },
            onDismiss = {
                viewModel.hideRecipeSelectionDialog()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with title and week navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_meal_plan),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            WeekSelector(
                currentWeek = uiState.week.toString(),
                onPreviousWeek = {
                    viewModel.previousWeek()
                },
                onNextWeek = {
                    viewModel.nextWeek()
                },
                onDateSelected = { selectedWeek: String ->
                    viewModel.updateWeek(LocalDate.parse(selectedWeek))
                }
            )
        }

        // Meal plan list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.mealPlans) { plan ->
                DayMealPlanCard(
                    mealPlan = plan,
                    onMealClick = { mealType ->
                        // Show recipe selection dialog
                        viewModel.showRecipeSelectionDialog(plan.date, mealType)
                    },
                    onRemoveRecipe = { recipeId ->
                        // Remove recipe from meal plan
                        viewModel.removeRecipeFromMealPlan(plan.date, recipeId)
                    }
                )
            }

            // Add spacing at the bottom for navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}