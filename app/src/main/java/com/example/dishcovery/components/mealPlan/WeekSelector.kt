package com.example.dishcovery.components.mealPlan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dishcovery.ui.theme.DishcoveryTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelector(
    currentWeek: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = modifier.height(40.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondary,
            onClick = { showDatePicker = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        showDatePicker = false
                        onPreviousWeek()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous week",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = currentWeek,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = {
                        showDatePicker = false
                        onNextWeek()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next week",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Date(millis)
                                val calendar = Calendar.getInstance().apply {
                                    time = selectedDate
                                }

                                // Calculate week range
                                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                                val startDate = calendar.time

                                calendar.add(Calendar.DAY_OF_WEEK, 6)
                                val endDate = calendar.time

                                val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                                val weekRange = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"

                                onDateSelected(weekRange)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeekSelectorPreview() {
    DishcoveryTheme {
        WeekSelector(
            currentWeek = "Nov 3 - 9",
            onPreviousWeek = {},
            onNextWeek = {},
            onDateSelected = {}
        )
    }
}
