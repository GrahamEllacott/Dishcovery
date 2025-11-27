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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dishcovery.data.models.ShoppingCategory
import com.example.dishcovery.data.models.ShoppingItem
import com.example.dishcovery.ui.theme.DishcoveryTheme
import com.example.dishcovery.ui.theme.TextPrimary

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier
) {
    // Sample data - to be replaced with ViewModel data
    val categories = remember {
        listOf(
            ShoppingCategory(
                id = 1,
                name = "Produce",
                emoji = "🥬",
                items = mutableListOf(
                    ShoppingItem(1, "Tomatoes", "4pcs", true),
                    ShoppingItem(2, "Lettuce", "1 head", true),
                    ShoppingItem(3, "Onions", "2 pcs", false),
                    ShoppingItem(4, "Garlic", "1 bulb", false),
                    ShoppingItem(5, "Bell Peppers", "3 pcs", true)
                )
            ),
            ShoppingCategory(
                id = 2,
                name = "Protein",
                emoji = "🥩",
                items = mutableListOf(
                    ShoppingItem(6, "Chicken Breast", "500g", true),
                    ShoppingItem(7, "Ground Beef", "750g", true),
                    ShoppingItem(8, "Eggs", "1 dozen", false),
                    ShoppingItem(9, "Tofu", "400g", false),
                    ShoppingItem(10, "Salmon", "160g", true)
                )
            ),
            ShoppingCategory(
                id = 3,
                name = "Dairy",
                emoji = "🧀",
                items = mutableListOf(
                    ShoppingItem(11, "Milk", "2L", false),
                    ShoppingItem(12, "Cheese", "200g", false),
                    ShoppingItem(13, "Yogurt", "500g", true)
                )
            ),
            ShoppingCategory(
                id = 4,
                name = "Pantry",
                emoji = "🥫",
                items = mutableListOf(
                    ShoppingItem(14, "Rice", "1kg", false),
                    ShoppingItem(15, "Pasta", "500g", true),
                    ShoppingItem(16, "Olive Oil", "250ml", false)
                )
            )
        ).toMutableStateList()
    }

    val customItemInput = remember { mutableStateOf("") }

    // Calculate progress
    val totalItems = categories.sumOf { it.items.size }
    val checkedItems = categories.sumOf { category ->
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
                    // Clear all checked items
                    categories.forEachIndexed { categoryIndex, category ->
                        val itemsToRemove = category.items.filter { it.isChecked }
                        itemsToRemove.forEach { item ->
                            categories[categoryIndex].items.remove(item)
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Clear All",
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
                    // Add to Pantry category by default
                    val pantryCategory = categories.find { it.name == "Pantry" }
                    pantryCategory?.items?.add(
                        ShoppingItem(
                            id = System.currentTimeMillis().toInt(),
                            name = customItemInput.value,
                            quantity = "",
                            isChecked = false
                        )
                    )
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
                items = categories,
                key = { _, category -> category.id }
            ) { categoryIndex, category ->
                ShoppingCategoryCard(
                    category = category,
                    onItemCheckedChange = { item, checked ->
                        val itemIndex = category.items.indexOfFirst { it.id == item.id }
                        if (itemIndex != -1) {
                            // Create a new item with updated checked state
                            val updatedItem = item.copy(isChecked = checked)
                            // Update the item in the list
                            categories[categoryIndex].items[itemIndex] = updatedItem
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingListScreenPreview() {
    DishcoveryTheme {
        ShoppingListScreen()
    }
}
