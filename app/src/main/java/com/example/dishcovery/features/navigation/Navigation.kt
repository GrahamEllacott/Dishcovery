package com.example.dishcovery.features.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.dishcovery.features.screens.AddEditRecipeScreen
import com.example.dishcovery.features.screens.DashboardScreen
import com.example.dishcovery.features.screens.RecipeDetailScreen
import com.example.dishcovery.features.screens.RecipeListScreen
import com.example.dishcovery.features.screens.ShoppingListScreen
import com.example.dishcovery.features.screens.SplashScreen
import com.example.dishcovery.features.screens.WeeklyMealPlanScreen
import com.example.dishcovery.features.viewmodels.RecipeListViewModel
import androidx.compose.runtime.collectAsState
import com.example.dishcovery.features.viewmodels.RecipeDetailViewModel


@Composable
fun AppEntryPoint() {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = "splash_route"
    ) {
        // 1. The Splash Screen Route
        composable("splash_route") {
            SplashScreen(
                onNavigateToMain = {
                    rootNavController.navigate("main_graph_route") {
                        popUpTo("splash_route") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 2. The Main App Route
        composable("main_graph_route") {
            MainNavigation()
        }
    }
}


sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Recipe : Screen("recipe", "Recipe", Icons.Outlined.Restaurant)
    object Plan : Screen("plan", "Plan", Icons.Outlined.CalendarToday)
    object Shopping : Screen("shopping", "Shopping", Icons.Outlined.ShoppingCart)
    object RecipeDetail : Screen("recipe_detail/{recipeId}", "Recipe Detail")
    object AddEditRecipe : Screen("add_edit_recipe?recipeId={recipeId}", "Add/Edit Recipe")
}

fun Screen.RecipeDetail.createRoute(recipeId: String): String {
    return "recipe_detail/$recipeId"
}

fun Screen.AddEditRecipe.createRoute(recipeId: String = ""): String {
    return if (recipeId != null) "add_edit_recipe?recipeId=$recipeId" else "add_edit_recipe"
}

@SuppressLint("UnrememberedGetBackStackEntry")
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
            currentRoute != Screen.RecipeDetail.route &&
            !currentRoute.toString().startsWith("add_edit_recipe")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
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
                                        MaterialTheme.colorScheme.onSurfaceVariant
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
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                DashboardScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    }
                )
            }
            composable(Screen.Recipe.route) {
                val viewModel: RecipeListViewModel = viewModel()
                RecipeListScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    },
                    onAddRecipeClick = {
                        navController.navigate(Screen.AddEditRecipe.createRoute())
                    }
                )
            }
            composable(Screen.Plan.route) {
                WeeklyMealPlanScreen()
            }
            composable(Screen.Shopping.route) {
                ShoppingListScreen()
            }
            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""

                RecipeDetailScreen(
                    recipeId = recipeId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        navController.navigate(Screen.AddEditRecipe.createRoute(recipeId))
                    },
                    onDeleteSuccess = {
                        // Navigate to Dashboard (Home) after successful deletion
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                            }
                        }
                    }
                )
            }
            composable(
                route = Screen.AddEditRecipe.route,
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")

                AddEditRecipeScreenWithPermissions(
                    recipeId = recipeId, // Pass recipeId instead of recipe object
                    onSaveClick = {
                        navController.popBackStack()
                    },
                    onCancelClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Wrapper composable that handles permissions for AddEditRecipeScreen
 */
@Composable
fun AddEditRecipeScreenWithPermissions(
    recipeId: String?,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(false) }

    // Define required permissions based on Android version
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResults ->
        hasPermissions = permissionResults.values.all { it }
    }

    // Check permissions on first composition
    LaunchedEffect(Unit) {
        hasPermissions = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        // Request permissions if not granted
        if (!hasPermissions) {
            permissionLauncher.launch(permissions)
        }
    }

    // Pass recipeId to AddEditRecipeScreen
    AddEditRecipeScreen(
        recipeId = recipeId, // Pass recipeId
        onSaveClick = onSaveClick,
        onCancelClick = onCancelClick
    )
}