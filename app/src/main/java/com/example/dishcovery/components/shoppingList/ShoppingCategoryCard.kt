import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.dishcovery.data.models.ShoppingCategory
import com.example.dishcovery.data.models.ShoppingItem
import com.example.dishcovery.ui.theme.DishcoveryTheme
import androidx.compose.ui.res.stringResource
import com.example.dishcovery.R

@Composable
fun ShoppingCategoryCard(
    category: ShoppingCategory,
    onItemCheckedChange: (ShoppingItem, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate progress for display
    val checkedCount = category.items.count { it.isChecked }
    val totalCount = category.items.size

    // Card container with secondary color theme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category header with emoji, name, and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category emoji and name
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                // Progress counter (e.g., "2/5")
                Text(
                    text = stringResource(R.string.category_progress, checkedCount, totalCount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of shopping items in this category
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                category.items.forEach { item ->
                    ShoppingItemRow(
                        item = item,
                        onCheckedChange = { checked ->
                            onItemCheckedChange(item, checked) // Forward callback
                        }
                    )
                }
            }
        }
    }
}

// Sample preview with produce category
@Preview(showBackground = true)
@Composable
fun ShoppingCategoryCardPreview() {
    DishcoveryTheme {
        ShoppingCategoryCard(
            category = ShoppingCategory(
                id = 1,
                name = "Produce",
                emoji = "🥬",
                items = mutableListOf(
                    ShoppingItem(1, "Tomatoes", "4pcs", true),
                    ShoppingItem(2, "Lettuce", "1 head", false),
                    ShoppingItem(3, "Onions", "2 pcs", false)
                )
            ),
            onItemCheckedChange = { _, _ -> }
        )
    }
}
