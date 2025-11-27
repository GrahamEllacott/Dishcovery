package com.example.dishcovery.features.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dishcovery.R
import com.example.dishcovery.data.models.Recipe
import com.example.dishcovery.features.screens.DashboardScreen
import com.example.dishcovery.features.screens.RecipeDetailScreen
import com.example.dishcovery.features.screens.RecipeListScreen
import com.example.dishcovery.features.screens.SplashScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Recipe : Screen("recipe", "Recipe", Icons.Outlined.Restaurant)
    object Plan : Screen("plan", "Plan", Icons.Outlined.CalendarToday)
    object Shopping : Screen("shopping", "Shopping", Icons.Outlined.ShoppingCart)
    object RecipeDetail : Screen("recipe_detail/{recipeId}", "Recipe Detail")
}

fun Screen.RecipeDetail.createRoute(recipeId: Int): String {
    return "recipe_detail/$recipeId"
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.Home,
        Screen.Recipe,
        Screen.Plan,
        Screen.Shopping
    )

    val showBottomBar = currentRoute != Screen.Splash.route &&
                        currentRoute != Screen.RecipeDetail.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Gray
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToMain = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                DashboardScreen()
            }
            composable(Screen.Recipe.route) {
                RecipeListScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    }
                )
            }
            composable(Screen.Plan.route) {
                // MealPlanScreen will be added here
                Text("Meal Plan Screen")
            }
            composable(Screen.Shopping.route) {
                // ShoppingListScreen will be added here
                Text("Shopping List Screen")
            }
            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0

                // Using sample date for now - to be replaced with actual data fetching
                val recipeDetail = Recipe(
                    id = recipeId,
                    name = "Spaghetti Carbonara",
                    imageRes = R.drawable.ic_launcher_background,
                    prepTime = 15,
                    cookTime = 10,
                    calories = 520,
                    category = "Dinner",
                    isFavorite = true,
                    protein = 28,
                    carbs = 58,
                    fat = 19,
                    fiber = 3,
                    sodium = 680,
                    ingredients = listOf(
                        "400g spaghetti",
                        "200g pancetta or bacon, diced",
                        "4 large eggs",
                        "100g Parmesan cheese, grated",
                        "2 cloves garlic, minced",
                        "Salt and black pepper to taste",
                        "Fresh parsley for garnish"
                    ),
                    instructions = listOf(
                        "Bring a large pot of salted water to boil. Cook spaghetti according to package directions until al dente.",
                        "While pasta cooks, heat a large skillet over medium heat. Add pancetta and cook until crispy, about 5-7 minutes.",
                        "In a bowl, whisk together eggs, Parmesan cheese, and a generous amount of black pepper.",
                        "Drain pasta, reserving 1 cup of pasta water. Add hot pasta to the skillet with pancetta.",
                        "Remove from heat and quickly stir in the egg mixture, tossing constantly. Add pasta water as needed to create a creamy sauce.",
                        "Season with salt and more pepper. Garnish with parsley and extra Parmesan."
                    )
                )

                RecipeDetailScreen(
                    recipe = recipeDetail,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { /* Navigate to edit screen */ },
                    onDeleteClick = { /* Handle delete */ },
                    onAddToMealPlan = { /* Handle add to meal plan */ }
                )
            }
        }
    }
}
