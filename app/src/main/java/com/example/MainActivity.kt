package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalDensity

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          CalculatorScreen(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "Logo", tint = MaterialTheme.colorScheme.onPrimary)
                }
                Column {
                    Text("Калькулятор вклада", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("com.antonlosk.calcdeposits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Result Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "ИТОГОВАЯ СУММА",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatter.format(state.finalAmount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Доход", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("+${formatter.format(state.profit)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        
                        if (state.calculatedEffectiveRate != null) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Эффективная ставка", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(String.format(Locale("ru", "RU"), "%.2f%%", state.calculatedEffectiveRate), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    
                    if (state.growthData.size > 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("ГРАФИК РОСТА", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        DepositChart(
                            data = state.growthData, 
                            startDate = state.startDate,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                    }
                }
            }

            // Inputs
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var showDatePicker by remember { mutableStateOf(false) }
                
                @OptIn(ExperimentalMaterial3Api::class)
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = state.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                    viewModel.onStartDateChanged(newDate)
                                }
                                showDatePicker = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Отмена")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                OutlinedTextField(
                    value = state.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ДАТА ОТКРЫТИЯ") },
                    modifier = Modifier.fillMaxWidth().testTag("input_start_date").clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = false // so click works on parent
                )

                OutlinedTextField(
                    value = state.initialAmount,
                    onValueChange = viewModel::onInitialAmountChanged,
                    label = { Text("СУММА ВКЛАДА (₽)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = state.term,
                            onValueChange = viewModel::onTermChanged,
                            label = { Text(if (state.isTermInYears) "СРОК (ЛЕТ)" else "СРОК (МЕС.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("input_term"),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            FilterChip(
                                selected = !state.isTermInYears,
                                onClick = { viewModel.onTermTypeChanged(false) },
                                label = { Text("Мес.") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = state.isTermInYears,
                                onClick = { viewModel.onTermTypeChanged(true) },
                                label = { Text("Лет") }
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = state.interestRate,
                            onValueChange = viewModel::onInterestRateChanged,
                            label = { Text("СТАВКА (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("input_rate"),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            FilterChip(
                                selected = !state.isEffectiveRate,
                                onClick = { viewModel.onRateTypeChanged(false) },
                                label = { Text("Ном.") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = state.isEffectiveRate,
                                onClick = { viewModel.onRateTypeChanged(true) },
                                label = { Text("Эфф.") }
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = !state.isEffectiveRate) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Капитализация процентов", color = MaterialTheme.colorScheme.onBackground)
                            Switch(
                                checked = state.isCapitalization,
                                onCheckedChange = viewModel::onCapitalizationChanged,
                                modifier = Modifier.testTag("switch_capitalization"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Navigation (Decorative)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(icon = Icons.Default.Calculate, label = "Главная", isSelected = true)
                NavigationItem(icon = Icons.Default.Search, label = "История", isSelected = false)
                NavigationItem(icon = Icons.Default.AutoAwesome, label = "Инфо", isSelected = false)
            }
        }
    }
}

@Composable
private fun NavigationItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = label, tint = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun DepositChart(
    data: List<ChartPoint>,
    startDate: LocalDate,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val tooltipColor = MaterialTheme.colorScheme.surfaceVariant
    val tooltipTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val profitColor = MaterialTheme.colorScheme.tertiary
    
    val textMeasurer = rememberTextMeasurer()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply { 
        maximumFractionDigits = 0 
    } }
    
    val lineAnimationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        lineAnimationProgress.snapTo(0f)
        lineAnimationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing))
    }
    
    var touchX by remember { mutableStateOf<Float?>(null) }
    
    val leftPadding = 64.dp
    val bottomPadding = 32.dp
    val topPadding = 32.dp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(data) {
                detectDragGestures(
                    onDragStart = { offset -> touchX = offset.x },
                    onDragEnd = { touchX = null },
                    onDragCancel = { touchX = null },
                    onDrag = { change, _ -> touchX = change.position.x }
                )
            }
            .pointerInput(data) {
                detectTapGestures(
                    onPress = { offset ->
                        touchX = offset.x
                        tryAwaitRelease()
                        touchX = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val leftPaddingPx = leftPadding.toPx()
            val bottomPaddingPx = bottomPadding.toPx()
            val topPaddingPx = topPadding.toPx()
            
            val chartWidth = size.width - leftPaddingPx
            val chartHeight = size.height - topPaddingPx - bottomPaddingPx
            
            val maxAmount = data.maxOfOrNull { it.amount }?.toFloat() ?: 0f
            val minAmount = data.minOfOrNull { it.amount }?.toFloat() ?: 0f
            
            val valuePadding = (maxAmount - minAmount) * 0.1f
            val maxValue = maxAmount + valuePadding
            val minValue = maxOf(0f, minAmount - valuePadding)
            val range = maxValue - minValue
            
            // 1. Draw Grid and Y-Axis Labels
            repeat(5) { i ->
                val y = topPaddingPx + chartHeight * (1f - i / 4f)
                val value = minValue + range * (i / 4f)
                
                drawLine(
                    color = gridColor,
                    start = Offset(leftPaddingPx, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                val labelText = formatter.format(value).replace(",00", "").replace(",0", "")
                val textLayoutResult = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(color = textColor, fontSize = 10.sp)
                )
                
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = leftPaddingPx - textLayoutResult.size.width - 8.dp.toPx(),
                        y = y - textLayoutResult.size.height / 2f
                    )
                )
            }
            
            val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
            
            val path = Path()
            val fillPath = Path()
            
            data.forEachIndexed { index, point ->
                val x = leftPaddingPx + index * stepX
                val normalizedY = if (range == 0f) 0.5f else (point.amount.toFloat() - minValue) / range
                val y = topPaddingPx + chartHeight * (1f - normalizedY)
                
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, topPaddingPx + chartHeight)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                
                if (index == data.lastIndex) {
                    fillPath.lineTo(x, topPaddingPx + chartHeight)
                    fillPath.close()
                }
            }
            
            clipRect(right = leftPaddingPx + chartWidth * lineAnimationProgress.value) {
                // 2. Draw Gradient Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.4f), primaryColor.copy(alpha = 0.0f)),
                        startY = topPaddingPx,
                        endY = topPaddingPx + chartHeight
                    )
                )
                
                // 3. Draw Line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )
                
                // 4. Draw X-Axis Labels (Start and End)
                val labelIndices = listOf(0, data.lastIndex).distinct()
                labelIndices.forEach { index ->
                    val point = data[index]
                    val x = leftPaddingPx + index * stepX
                    val dateStr = startDate.plusMonths(point.monthsFromStart.toLong()).format(dateFormatter)
                    
                    val textLayout = textMeasurer.measure(
                        text = dateStr,
                        style = TextStyle(color = textColor, fontSize = 10.sp)
                    )
                    
                    val textX = when (index) {
                        0 -> x
                        data.lastIndex -> x - textLayout.size.width
                        else -> x - textLayout.size.width / 2f
                    }
                    
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(textX, topPaddingPx + chartHeight + 8.dp.toPx())
                    )
                }
                
                // 5. Interactive or Default Marker
                val selectedIndex = touchX?.let { tx ->
                    if (tx >= leftPaddingPx) {
                        ((tx - leftPaddingPx) / stepX).roundToInt().coerceIn(0, data.lastIndex)
                    } else null
                }
                
                if (selectedIndex != null && touchX != null) {
                    // Interactive mode
                    val point = data[selectedIndex]
                    val x = leftPaddingPx + selectedIndex * stepX
                    val normalizedY = if (range == 0f) 0.5f else (point.amount.toFloat() - minValue) / range
                    val y = topPaddingPx + chartHeight * (1f - normalizedY)
                    
                    // Vertical line
                    drawLine(
                        color = textColor.copy(alpha = 0.5f),
                        start = Offset(x, topPaddingPx),
                        end = Offset(x, topPaddingPx + chartHeight),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    
                    // Marker
                    drawCircle(color = surfaceColor, radius = 6.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
                    
                    // Tooltip
                    val dateStr = startDate.plusMonths(point.monthsFromStart.toLong()).format(dateFormatter)
                    val amountStr = formatter.format(point.amount).replace(",00", "").replace(",0", "")
                    val profitStr = "Прибыль: +${formatter.format(point.profit).replace(",00", "").replace(",0", "")}"
                    
                    val dateLabel = textMeasurer.measure(dateStr, style = TextStyle(color = tooltipTextColor, fontSize = 10.sp))
                    val amountLabel = textMeasurer.measure(amountStr, style = TextStyle(color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    val profitLabel = textMeasurer.measure(profitStr, style = TextStyle(color = profitColor, fontSize = 10.sp))
                    
                    val tooltipWidth = maxOf(dateLabel.size.width, amountLabel.size.width, profitLabel.size.width) + 32.dp.toPx()
                    val tooltipHeight = dateLabel.size.height + amountLabel.size.height + profitLabel.size.height + 24.dp.toPx()
                    
                    var tooltipX = x - tooltipWidth / 2f
                    if (tooltipX < leftPaddingPx) tooltipX = leftPaddingPx
                    if (tooltipX + tooltipWidth > size.width) tooltipX = size.width - tooltipWidth
                    
                    var tooltipY = y - tooltipHeight - 16.dp.toPx()
                    if (tooltipY < 0f) tooltipY = y + 16.dp.toPx()
                    
                    drawRoundRect(
                        color = tooltipColor,
                        topLeft = Offset(tooltipX, tooltipY),
                        size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                    
                    drawText(dateLabel, topLeft = Offset(tooltipX + 16.dp.toPx(), tooltipY + 8.dp.toPx()))
                    drawText(amountLabel, topLeft = Offset(tooltipX + 16.dp.toPx(), tooltipY + 8.dp.toPx() + dateLabel.size.height))
                    drawText(profitLabel, topLeft = Offset(tooltipX + 16.dp.toPx(), tooltipY + 8.dp.toPx() + dateLabel.size.height + amountLabel.size.height + 4.dp.toPx()))
                    
                } else if (lineAnimationProgress.value == 1f) {
                    // Default state (only last marker)
                    val point = data.last()
                    val x = leftPaddingPx + data.lastIndex * stepX
                    val normalizedY = if (range == 0f) 0.5f else (point.amount.toFloat() - minValue) / range
                    val y = topPaddingPx + chartHeight * (1f - normalizedY)
                    
                    drawCircle(color = surfaceColor, radius = 6.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
                    
                    val amountStr = formatter.format(point.amount).replace(",00", "").replace(",0", "")
                    val amountLabel = textMeasurer.measure(amountStr, style = TextStyle(color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    
                    drawText(
                        amountLabel,
                        topLeft = Offset(
                            x = x - amountLabel.size.width - 12.dp.toPx(),
                            y = y - amountLabel.size.height / 2f
                        )
                    )
                }
            }
        }
    }
}