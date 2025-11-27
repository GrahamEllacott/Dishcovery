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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dishcovery.R
import com.example.dishcovery.components.mealPlan.DayMealPlanCard
import com.example.dishcovery.components.mealPlan.WeekSelector
import com.example.dishcovery.data.models.DayMealPlan
import com.example.dishcovery.data.models.MealPlanItem
import com.example.dishcovery.ui.theme.DishcoveryTheme
import com.example.dishcovery.ui.theme.TextPrimary

@Composable
fun WeeklyMealPlanScreen(
    onNavigateToRecipes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Sample data - to be replaced with ViewModel data
    var currentWeek by remember { mutableStateOf("Nov 3 - 9") }

    val weeklyMealPlan = remember {
        listOf(
            DayMealPlan(
                dayName = "Monday",
                date = "Nov 3",
                meals = listOf(
                    MealPlanItem(1, "Breakfast", "Toast with egg", "8:00AM", 400, R.drawable.ic_launcher_background),
                    MealPlanItem(2, "Lunch", "French Grated Carrot Salad", "12:30PM", 500, R.drawable.ic_launcher_background),
                    MealPlanItem(3, "Dinner", "Salmon Steak", "7:00PM", 700, R.drawable.ic_launcher_background)
                )
            ),
            DayMealPlan(
                dayName = "Tuesday",
                date = "Nov 4",
                meals = listOf(
                    MealPlanItem(4, "Breakfast", "Greek yogurt", "8:00AM", 300, R.drawable.ic_launcher_background),
                    null, // Empty lunch slot
                    MealPlanItem(5, "Dinner", "Steak", "7:00PM", 700, R.drawable.ic_launcher_background)
                )
            ),
            DayMealPlan(
                dayName = "Wednesday",
                date = "Nov 5",
                meals = listOf(null, null, null) // All empty slots
            ),
            DayMealPlan(
                dayName = "Thursday",
                date = "Nov 6",
                meals = listOf(null, null, null)
            ),
            DayMealPlan(
                dayName = "Friday",
                date = "Nov 7",
                meals = listOf(null, null, null)
            ),
            DayMealPlan(
                dayName = "Saturday",
                date = "Nov 8",
                meals = listOf(null, null, null)
            ),
            DayMealPlan(
                dayName = "Sunday",
                date = "Nov 9",
                meals = listOf(null, null, null)
            )
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
                text = "Meal Plan",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            WeekSelector(
                currentWeek = currentWeek,
                onPreviousWeek = {
                    // For now, just a placeholder
                },
                onNextWeek = {
                    // For now, just a placeholder
                },
                onDateSelected = { selectedWeek: String ->
                    // Update the current week when date is selected
                    currentWeek = selectedWeek
                }
            )
        }

        // Meal plan list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(weeklyMealPlan) { dayPlan ->
                DayMealPlanCard(
                    dayPlan = dayPlan,
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
