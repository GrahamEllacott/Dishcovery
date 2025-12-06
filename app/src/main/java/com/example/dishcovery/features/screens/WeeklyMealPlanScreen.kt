package com.example.dishcovery.features.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dishcovery.components.mealPlan.DayMealPlanCard
import com.example.dishcovery.components.mealPlan.WeekSelector
import com.example.dishcovery.features.viewmodels.WeeklyMealPlanViewModel
import com.example.dishcovery.ui.theme.DishcoveryTheme
import java.time.LocalDate

@Composable
fun WeeklyMealPlanScreen(
    onNavigateToRecipes: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: WeeklyMealPlanViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

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
                text = "Meal Plan",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            WeekSelector(
                currentWeek = uiState.week.toString(),
                onPreviousWeek = {
                    // For now, just a placeholder
                    viewModel.previousWeek()
                },
                onNextWeek = {
                    // For now, just a placeholder
                    viewModel.nextWeek()
                },
                onDateSelected = { selectedWeek: String ->
                    // Update the current week when date is selected
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
                        // Navigate to recipes page for now
                        onNavigateToRecipes()
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

@Preview(showBackground = true)
@Composable
fun WeeklyMealPlanScreenPreview() {
    DishcoveryTheme {
        WeeklyMealPlanScreen()
    }
}
