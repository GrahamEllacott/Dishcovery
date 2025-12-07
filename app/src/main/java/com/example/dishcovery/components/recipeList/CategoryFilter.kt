package com.example.dishcovery.components.recipeList

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
@Composable
fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                },
                shape = RoundedCornerShape(50.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == category,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    borderWidth = 2.dp,
                    selectedBorderWidth = 2.dp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryFilterPreview() {
    val categories = listOf("All", "Italian", "Mexican", "Chinese")

    CategoryFilter(
        categories = categories,
        selectedCategory = categories[0],
        onCategorySelected = {},
    )
}
