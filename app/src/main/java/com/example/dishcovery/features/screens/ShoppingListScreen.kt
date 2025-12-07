package com.example.dishcovery.features.screens

import AddItemInput
import ProgressHeader
import ShoppingCategoryCard
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dishcovery.features.viewmodels.ShoppingListViewModel

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val customItemInput = remember { mutableStateOf("") }

    // Initialize repository
    LaunchedEffect(Unit) {
        viewModel.initRepository(context)
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Calculate progress
    val totalItems = uiState.categories.sumOf { it.items.size }
    val checkedItems = uiState.categories.sumOf { category ->
        category.items.count { it.isChecked }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping List",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedButton(
                onClick = {
                    viewModel.toggleHideChecked()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (uiState.hideChecked) "Show All" else "Hide Checked",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Progress indicator
        ProgressHeader(
            checkedItems = checkedItems,
            totalItems = totalItems
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add custom item input
        AddItemInput(
            value = customItemInput.value,
            onValueChange = { customItemInput.value = it },
            onAddClick = {
                if (customItemInput.value.isNotBlank()) {
                    viewModel.addCustomItem(customItemInput.value)
                    customItemInput.value = ""
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Shopping list categories
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = uiState.categories,
                key = { _, category -> category.id }
            ) { categoryIndex, category ->
                ShoppingCategoryCard(
                    category = category,
                    onItemCheckedChange = { item, checked ->
                        viewModel.toggleItem(category.id, item.id, checked)
                    }
                )
            }
        }

        // Snackbar host
        SnackbarHost(hostState = snackbarHostState)
    }
}