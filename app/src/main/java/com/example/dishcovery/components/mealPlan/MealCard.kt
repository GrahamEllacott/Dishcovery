package com.example.dishcovery.components.mealPlan

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dishcovery.R
import com.example.dishcovery.data.models.MealPlanItem
import com.example.dishcovery.ui.theme.DishcoveryTheme
import com.example.dishcovery.ui.theme.TextPrimary
import com.example.dishcovery.ui.theme.TextSecondary

@Composable
fun MealCard(
    meal: MealPlanItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal image
            Image(
                painter = painterResource(id = meal.imageRes),
                contentDescription = meal.recipeName,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Meal details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${meal.mealType} ${meal.time}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = meal.recipeName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${meal.calories}kcal",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MealCardPreview() {
    DishcoveryTheme {
        MealCard(
            meal = MealPlanItem(
                id = 1,
                mealType = "Breakfast",
                recipeName = "Toast with egg",
                time = "8:00AM",
                calories = 400,
                imageRes = R.drawable.ic_launcher_background
            ),
            onClick = {}
        )
    }
}
