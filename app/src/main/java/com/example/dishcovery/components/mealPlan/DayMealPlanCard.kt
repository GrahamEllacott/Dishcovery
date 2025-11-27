package com.example.dishcovery.components.mealPlan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dishcovery.R
import com.example.dishcovery.data.models.DayMealPlan
import com.example.dishcovery.data.models.MealPlanItem
import com.example.dishcovery.ui.theme.DishcoveryTheme
import com.example.dishcovery.ui.theme.TextPrimary
import com.example.dishcovery.ui.theme.TextSecondary

@Composable
fun DayMealPlanCard(
    dayPlan: DayMealPlan,
    onMealClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Day header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayPlan.dayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = dayPlan.date,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meals
            val mealTypes = listOf("Breakfast", "Lunch", "Dinner")
            mealTypes.forEachIndexed { index, mealType ->
                val meal = dayPlan.meals.getOrNull(index)

                if (meal != null) {
                    MealCard(
                        meal = meal,
                        onClick = { onMealClick(mealType) }
                    )
                } else {
                    EmptyMealSlot(
                        mealType = mealType,
                        onClick = { onMealClick(mealType) }
                    )
                }

                if (index < mealTypes.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DayMealPlanCardPreview() {
    DishcoveryTheme {
        DayMealPlanCard(
            dayPlan = DayMealPlan(
                dayName = "Monday",
                date = "Nov 3",
                meals = listOf(
                    MealPlanItem(1, "Breakfast", "Toast with egg", "8:00AM", 400, R.drawable.ic_launcher_background),
                    null,
                    MealPlanItem(3, "Dinner", "Salmon Steak", "7:00PM", 700, R.drawable.ic_launcher_background)
                )
            ),
            onMealClick = {}
        )
    }
}
