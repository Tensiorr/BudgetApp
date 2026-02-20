package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Custom date picker dialog with calendar view and manual input.
 */
@Composable
fun CustomDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var showManualInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wybierz datę",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    IconButton(onClick = { showManualInput = !showManualInput }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Wpisz ręcznie",
                            tint = if (showManualInput) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (showManualInput) {
                    DateInputField(
                        date = selectedDate,
                        onDateChange = { newDate ->
                            selectedDate = newDate
                            currentMonth = YearMonth.from(newDate)
                        },
                        label = "Data",
                        dateFormat = dateFormat,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CalendarView(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        onMonthChange = { newMonth ->
                            currentMonth = newMonth
                        },
                        onDateClick = { date ->
                            selectedDate = date
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }

                    Spacer(Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            onDateSelected(selectedDate)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

/**
 * Calendar grid view with month navigation.
 */
@Composable
private fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Poprzedni miesiąc")
            }

            Text(
                text = currentMonth.format(
                    DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl", "PL"))
                ).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Następny miesiąc")
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dayNames = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Ni")
            dayNames.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value

        val paddingDays = firstDayOfWeek - 1
        val totalCells = paddingDays + daysInMonth

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(paddingDays) {
                Box(modifier = Modifier.size(40.dp))
            }

            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val date = currentMonth.atDay(day)
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                        )
                        .clickable { onDateClick(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun DateInputField(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    label: String = "Data",
    dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    modifier: Modifier = Modifier
) {
    var field1Text by remember(date, dateFormat) {
        mutableStateOf(
            when (dateFormat) {
                DateFormatOption.DD_MM_YYYY,
                DateFormatOption.DD_MM_YYYY_SLASH -> date.dayOfMonth.toString().padStart(2, '0')
                DateFormatOption.MM_DD_YYYY,
                DateFormatOption.MM_DD_YYYY_SLASH -> date.monthValue.toString().padStart(2, '0')
                DateFormatOption.YYYY_MM_DD -> date.year.toString()
            }
        )
    }

    var field2Text by remember(date, dateFormat) {
        mutableStateOf(
            when (dateFormat) {
                DateFormatOption.DD_MM_YYYY,
                DateFormatOption.DD_MM_YYYY_SLASH -> date.monthValue.toString().padStart(2, '0')
                DateFormatOption.MM_DD_YYYY,
                DateFormatOption.MM_DD_YYYY_SLASH -> date.dayOfMonth.toString().padStart(2, '0')
                DateFormatOption.YYYY_MM_DD -> date.monthValue.toString().padStart(2, '0')
            }
        )
    }

    var field3Text by remember(date, dateFormat) {
        mutableStateOf(
            when (dateFormat) {
                DateFormatOption.DD_MM_YYYY,
                DateFormatOption.DD_MM_YYYY_SLASH,
                DateFormatOption.MM_DD_YYYY,
                DateFormatOption.MM_DD_YYYY_SLASH -> date.year.toString()
                DateFormatOption.YYYY_MM_DD -> date.dayOfMonth.toString().padStart(2, '0')
            }
        )
    }

    val (placeholder1, placeholder2, placeholder3) = when (dateFormat) {
        DateFormatOption.DD_MM_YYYY,
        DateFormatOption.DD_MM_YYYY_SLASH -> Triple("DD", "MM", "YYYY")
        DateFormatOption.MM_DD_YYYY,
        DateFormatOption.MM_DD_YYYY_SLASH -> Triple("MM", "DD", "YYYY")
        DateFormatOption.YYYY_MM_DD -> Triple("YYYY", "MM", "DD")
    }

    val separator = dateFormat.separator

    fun updateDate() {
        try {
            val (day, month, year) = when (dateFormat) {
                DateFormatOption.DD_MM_YYYY,
                DateFormatOption.DD_MM_YYYY_SLASH -> {
                    Triple(
                        field1Text.toIntOrNull()?.coerceIn(1, 31) ?: date.dayOfMonth,
                        field2Text.toIntOrNull()?.coerceIn(1, 12) ?: date.monthValue,
                        field3Text.toIntOrNull()?.coerceIn(1900, 2100) ?: date.year
                    )
                }
                DateFormatOption.MM_DD_YYYY,
                DateFormatOption.MM_DD_YYYY_SLASH -> {
                    Triple(
                        field2Text.toIntOrNull()?.coerceIn(1, 31) ?: date.dayOfMonth,
                        field1Text.toIntOrNull()?.coerceIn(1, 12) ?: date.monthValue,
                        field3Text.toIntOrNull()?.coerceIn(1900, 2100) ?: date.year
                    )
                }
                DateFormatOption.YYYY_MM_DD -> {
                    Triple(
                        field3Text.toIntOrNull()?.coerceIn(1, 31) ?: date.dayOfMonth,
                        field2Text.toIntOrNull()?.coerceIn(1, 12) ?: date.monthValue,
                        field1Text.toIntOrNull()?.coerceIn(1900, 2100) ?: date.year
                    )
                }
            }

            val maxDay = YearMonth.of(year, month).lengthOfMonth()
            val validDay = day.coerceIn(1, maxDay)

            val newDate = LocalDate.of(year, month, validDay)
            onDateChange(newDate)

            when (dateFormat) {
                DateFormatOption.DD_MM_YYYY,
                DateFormatOption.DD_MM_YYYY_SLASH -> {
                    if (validDay != day) {
                        field1Text = validDay.toString().padStart(2, '0')
                    }
                }
                DateFormatOption.MM_DD_YYYY,
                DateFormatOption.MM_DD_YYYY_SLASH -> {
                    if (validDay != day) {
                        field2Text = validDay.toString().padStart(2, '0')
                    }
                }
                DateFormatOption.YYYY_MM_DD -> {
                    if (validDay != day) {
                        field3Text = validDay.toString().padStart(2, '0')
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = field1Text,
                onValueChange = { newValue ->
                    val maxLen = if (placeholder1 == "YYYY") 4 else 2
                    if (newValue.length <= maxLen && newValue.all { it.isDigit() }) {
                        field1Text = newValue
                        if (newValue.length == maxLen) {
                            val value = newValue.toIntOrNull() ?: 1
                            val maxValue = when (placeholder1) {
                                "DD" -> 31
                                "MM" -> 12
                                else -> 9999
                            }
                            if (value > maxValue) {
                                field1Text = maxValue.toString().padStart(maxLen, '0')
                            }
                            updateDate()
                        }
                    }
                },
                modifier = Modifier.width(if (placeholder1 == "YYYY") 100.dp else 70.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true,
                placeholder = { Text(placeholder1, textAlign = TextAlign.Center) }
            )

            Text(separator, style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = field2Text,
                onValueChange = { newValue ->
                    if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                        field2Text = newValue
                        if (newValue.length == 2) {
                            val value = newValue.toIntOrNull() ?: 1
                            val maxValue = when (placeholder2) {
                                "DD" -> 31
                                "MM" -> 12
                                else -> 99
                            }
                            if (value > maxValue) {
                                field2Text = maxValue.toString().padStart(2, '0')
                            }
                            updateDate()
                        }
                    }
                },
                modifier = Modifier.width(70.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true,
                placeholder = { Text(placeholder2, textAlign = TextAlign.Center) }
            )

            Text(separator, style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = field3Text,
                onValueChange = { newValue ->
                    val maxLen = if (placeholder3 == "YYYY") 4 else 2
                    if (newValue.length <= maxLen && newValue.all { it.isDigit() }) {
                        field3Text = newValue
                        if (newValue.length == maxLen) {
                            val value = newValue.toIntOrNull() ?: 1
                            val maxValue = when (placeholder3) {
                                "DD" -> 31
                                "YYYY" -> 9999
                                else -> 99
                            }
                            if (value > maxValue) {
                                field3Text = maxValue.toString().padStart(maxLen, '0')
                            }
                            updateDate()
                        }
                    }
                },
                modifier = Modifier.width(if (placeholder3 == "YYYY") 100.dp else 70.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true,
                placeholder = { Text(placeholder3, textAlign = TextAlign.Center) }
            )
        }
    }
}