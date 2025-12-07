package com.example.dishcovery.components.mealPlan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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
                    text = formatWeekRange(currentWeek),
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
                                // Convert millis to LocalDate
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                // Get the Monday of the selected week
                                val monday = selectedDate.with(java.time.DayOfWeek.MONDAY)

                                // Return the Monday date as ISO format string
                                onDateSelected(monday.toString())
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

/**
 * Format the week range for display
 * Input: ISO date string like "2024-01-08" (Monday)
 * Output: "Jan 8 - 14" or "Dec 30 - Jan 5" for cross-month weeks
 */
private fun formatWeekRange(mondayDateString: String): String {
    return try {
        val monday = LocalDate.parse(mondayDateString)
        val sunday = monday.plusDays(6)

        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

        // Check if the week spans across months
        if (monday.month == sunday.month) {
            "${formatter.format(monday)} - ${sunday.dayOfMonth}"
        } else {
            "${formatter.format(monday)} - ${formatter.format(sunday)}"
        }
    } catch (e: Exception) {
        mondayDateString
    }
}