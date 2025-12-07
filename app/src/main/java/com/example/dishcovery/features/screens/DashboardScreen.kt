package com.example.dishcovery.features.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dishcovery.features.viewmodels.DashboardViewModel
import com.example.dishcovery.ui.theme.DishcoveryTheme

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with title and icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Notification Icon
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(onClick = { /* Notifications */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Profile Icon
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(onClick = { /* Profile */ }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }

            // Content Section

            // Today's Meals Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
            ) {
                Text(
                    "Today's Meals",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(18.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(bottom = 18.dp)
                ) {
                    items(uiState.todaysMeals) { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )

                            ) {
                            Row (
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = Uri.parse(recipe.imageUri),
                                    contentDescription = recipe.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(117.dp, 90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                
                                Column (
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        recipe.category,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        recipe.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "${recipe.calories}kcal",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    // if theres no meals, add a placeholder
                    if (uiState.todaysMeals.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )

                            ) {
                                Column (
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxSize()
                                ) {
                                    Text(
                                        "No meals today",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Let's get Cooking!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // Today's Nutrition Summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 8.dp),
            ) {
                Text(
                    "Today's Nutrition Summary",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row{
                            Text(
                                "Calories",
                                fontWeight = FontWeight.Bold,
                            )
                            Icon(
                                imageVector = Icons.Outlined.LocalFireDepartment,
                                contentDescription = "Calories",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Calories Graph
                            Box (
                                modifier = Modifier.padding(15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = {uiState.nutritionSummary.caloriesPercent},
                                    modifier = Modifier
                                        .size(105.dp)
                                        .scale(-1f, 1f),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 7.dp,
                                    trackColor = ProgressIndicatorDefaults.circularDeterminateTrackColor,
                                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ){
                                    Text(
                                        "${uiState.nutritionSummary.totalCalories} cals",
                                        fontSize = 14.sp,
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 0.6.em
                                        )
                                    )
                                    Text(
                                        "${uiState.nutritionSummary.caloriesPercent * 100}%",
                                        fontSize = 14.sp,
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 0.6.em
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(15.dp))

                            // Nutrition Bars
                            Column (
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center

                            ){
                                Row(){
                                    Text(
                                        "Fats",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "${uiState.nutritionSummary.fatsPercent * 100}% ${uiState.nutritionSummary.fatsGrams}g",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = {uiState.nutritionSummary.fatsPercent},
                                    modifier = Modifier.height(11.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                    drawStopIndicator = {}
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(){
                                    Text(
                                        "Protien",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "${uiState.nutritionSummary.proteinPercent * 100}% ${uiState.nutritionSummary.proteinGrams}g",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = {uiState.nutritionSummary.proteinPercent},
                                    modifier = Modifier.height(11.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                    drawStopIndicator = {}
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(){
                                    Text(
                                        "Carbs",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "${uiState.nutritionSummary.carbsPercent * 100}% ${uiState.nutritionSummary.carbsGrams}g",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = {uiState.nutritionSummary.carbsPercent},
                                    modifier = Modifier.height(11.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                    drawStopIndicator = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DishcoveryTheme {
        DashboardScreen()
    }
}