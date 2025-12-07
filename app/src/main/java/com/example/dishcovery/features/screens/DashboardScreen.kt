package com.example.dishcovery.features.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dishcovery.features.viewmodels.DashboardViewModel
import androidx.compose.ui.res.stringResource
import com.example.dishcovery.R

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onRecipeClick: (String) -> Unit = {}
) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Refresh data when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

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
                    text = stringResource(R.string.title_dashboard),
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
                                contentDescription = stringResource(R.string.cd_notifications),
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
                                contentDescription = stringResource(R.string.cd_profile),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }

            // Show loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Today's Meals Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 26.dp)
                        ) {
                            Text(
                                stringResource(R.string.today_meals),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }
                    }

                    // Meals List
                    if (uiState.todaysMeals.isNotEmpty()) {
                        items(uiState.todaysMeals) { recipe ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 26.dp, vertical = 9.dp)
                                    .height(90.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                onClick = { onRecipeClick(recipe.id) }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Recipe Image
                                    if (!recipe.imageUri.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = recipe.imageUri,
                                            contentDescription = recipe.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(117.dp, 90.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(117.dp, 90.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = recipe.name.take(1).uppercase(),
                                                fontSize = 40.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Column(
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
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            stringResource(R.string.calories_kcal_format, recipe.calories),
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty state
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 26.dp)
                                    .height(90.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxSize()
                                ) {
                                    Text(
                                        stringResource(R.string.no_meals_today),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        stringResource(R.string.lets_get_cooking),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Today's Nutrition Summary
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 26.dp, vertical = 18.dp),
                        ) {
                            Text(
                                stringResource(R.string.today_nutrition_summary),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row {
                                        Text(
                                            stringResource(R.string.calories),
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.LocalFireDepartment,
                                            contentDescription = stringResource(R.string.cd_calories),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Calories Graph
                                        Box(
                                            modifier = Modifier.padding(15.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { uiState.nutritionSummary.caloriesPercent },
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
                                            ) {
                                                Text(
                                                    stringResource(R.string.total_calories_format, uiState.nutritionSummary.totalCalories),
                                                    fontSize = 14.sp,
                                                    style = TextStyle(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        lineHeight = 0.6.em
                                                    )
                                                )
                                                Text(
                                                    stringResource(R.string.nutrition_percent_format, (uiState.nutritionSummary.caloriesPercent * 100).toInt()),
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
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            // Fats
                                            Row {
                                                Text(
                                                    stringResource(R.string.nutrition_fats),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    stringResource(R.string.nutrition_percent_grams_format, (uiState.nutritionSummary.fatsPercent * 100).toInt(), uiState.nutritionSummary.fatsGrams),
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { uiState.nutritionSummary.fatsPercent },
                                                modifier = Modifier.height(11.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                                                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                                drawStopIndicator = {}
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Protein
                                            Row {
                                                Text(
                                                    stringResource(R.string.nutrition_protein),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    stringResource(R.string.nutrition_percent_grams_format, (uiState.nutritionSummary.proteinPercent * 100).toInt(), uiState.nutritionSummary.proteinGrams),
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { uiState.nutritionSummary.proteinPercent },
                                                modifier = Modifier.height(11.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                                                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                                drawStopIndicator = {}
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Carbs
                                            Row {
                                                Text(
                                                    stringResource(R.string.nutrition_carbs),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    stringResource(R.string.nutrition_percent_grams_format, (uiState.nutritionSummary.carbsPercent * 100).toInt(), uiState.nutritionSummary.carbsGrams),
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { uiState.nutritionSummary.carbsPercent },
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
        }
    }
}

