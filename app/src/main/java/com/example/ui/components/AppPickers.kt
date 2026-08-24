package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    maxDateIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialLocalDate = try {
        LocalDate.parse(initialDateIso)
    } catch (_: Exception) {
        LocalDate.now()
    }
    
    val initialMillis = initialLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val todayMaxMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + (86400000 - 1)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayMaxMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selectedLocalDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        
                        val finalDate = if (selectedLocalDate.isAfter(LocalDate.now())) {
                            LocalDate.now()
                        } else {
                            selectedLocalDate
                        }
                        onDateSelected(finalDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    } else {
                        onDateSelected(initialDateIso)
                    }
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    initialTime: String = "08:00",
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parsedTime = try {
        val parts = initialTime.split(":")
        LocalTime.of(parts[0].toInt(), parts[1].toInt())
    } catch (_: Exception) {
        LocalTime.of(8, 0)
    }

    val timePickerState = rememberTimePickerState(
        initialHour = parsedTime.hour,
        initialMinute = parsedTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Time")
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formatted = String.format(
                        java.util.Locale.US,
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(formatted)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
