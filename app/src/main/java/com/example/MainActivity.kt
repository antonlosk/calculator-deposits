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
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.drawscope.clipRect
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
                    }
                    
                    if (state.growthData.size > 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("ГРАФИК РОСТА", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        DepositChart(
                            data = state.growthData, 
                            isTermInYears = state.isTermInYears,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                    }
                }
            }

            // Inputs
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                    OutlinedTextField(
                        value = state.interestRate,
                        onValueChange = viewModel::onInterestRateChanged,
                        label = { Text("СТАВКА (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_rate"),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

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
    data: List<Double>, 
    isTermInYears: Boolean,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    val textMeasurer = rememberTextMeasurer()
    val scrollState = rememberScrollState()

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply { 
        maximumFractionDigits = 0 
    } }

    val lineAnimationProgress = remember { Animatable(0f) }
    val pointsAnimationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        lineAnimationProgress.snapTo(0f)
        pointsAnimationProgress.snapTo(0f)
        lineAnimationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing))
        pointsAnimationProgress.animateTo(1f, animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val pointCount = data.size
    val minSpacing = 60.dp
    val density = LocalDensity.current
    val minWidthPx = with(density) { (minSpacing * pointCount.toFloat()).toPx() }
    val leftPadding = 72.dp
    val leftPaddingPx = with(density) { leftPadding.toPx() }

    Box(
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxAmountOrig = data.maxOrNull()?.toFloat() ?: 0f
            val minAmountOrig = data.minOrNull()?.toFloat() ?: 0f
            val rangeOrig = if (maxAmountOrig == minAmountOrig) 1f else maxAmountOrig - minAmountOrig
            
            val padding = rangeOrig * 0.1f
            val maxValue = maxAmountOrig + padding
            val minValue = maxOf(0f, minAmountOrig - padding)
            val range = maxValue - minValue

            val height = size.height
            val width = size.width
            val paddingY = height * 0.2f
            val chartHeight = height - paddingY * 1.5f

            repeat(5) { i ->
                val y = height - paddingY - chartHeight * (i / 4f)
                val value = minValue + range * (i / 4f)
                
                drawLine(
                    color = gridColor,
                    start = Offset(leftPaddingPx, y),
                    end = Offset(width, y),
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
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = leftPadding)
                .horizontalScroll(scrollState)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val stepX = maxOf(size.width, minWidthPx.toInt()) / (data.size - 1).coerceAtLeast(1).toFloat()
                        val index = (offset.x / stepX).roundToInt().coerceIn(0, data.lastIndex)
                        selectedIndex = index
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxHeight().width(maxOf(300.dp, with(density) { minWidthPx.toDp() }))) {
                val maxAmountOrig = data.maxOrNull()?.toFloat() ?: 0f
                val minAmountOrig = data.minOrNull()?.toFloat() ?: 0f
                val rangeOrig = if (maxAmountOrig == minAmountOrig) 1f else maxAmountOrig - minAmountOrig
                
                val padding = rangeOrig * 0.1f
                val maxValue = maxAmountOrig + padding
                val minValue = maxOf(0f, minAmountOrig - padding)
                val range = maxValue - minValue
                
                val width = size.width
                val height = size.height
                val stepX = width / (data.size - 1).coerceAtLeast(1).toFloat()
                
                val path = Path()
                val fillPath = Path()
                
                val paddingY = height * 0.2f
                val chartHeight = height - paddingY * 1.5f
                
                data.forEachIndexed { index, value ->
                    val x = index * stepX
                    val normalizedY = if (range == 0f) 0.5f else (value.toFloat() - minValue) / range
                    val y = height - paddingY - (normalizedY * chartHeight)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height - paddingY)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                    
                    if (index == data.lastIndex) {
                        fillPath.lineTo(x, height - paddingY)
                        fillPath.close()
                    }
                }
                
                clipRect(right = width * lineAnimationProgress.value) {
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.15f), Color.Transparent),
                            startY = 0f,
                            endY = height - paddingY
                        )
                    )
                    
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    data.forEachIndexed { index, value ->
                        val x = index * stepX
                        val normalizedY = if (range == 0f) 0.5f else (value.toFloat() - minValue) / range
                        val y = height - paddingY - (normalizedY * chartHeight)
                        
                        val isLast = index == data.lastIndex
                        val pointRadius = if (isLast) 6.dp.toPx() else 4.dp.toPx()
                        
                        if (pointsAnimationProgress.value > 0f) {
                            val currentRadius = pointRadius * pointsAnimationProgress.value
                            drawCircle(
                                color = androidx.compose.ui.graphics.Color.White, // Use white for inner circle in both themes, or fetch surface
                                radius = currentRadius + 1.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = currentRadius,
                                center = Offset(x, y)
                            )
                        }
                        
                        val labelText = if (isTermInYears) "$index л" else "$index м"
                        val textLayoutResult = textMeasurer.measure(
                            text = labelText,
                            style = TextStyle(color = textColor, fontSize = 10.sp)
                        )
                        
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = x - textLayoutResult.size.width / 2f,
                                y = height - paddingY + 8.dp.toPx()
                            )
                        )
                        
                        if (isLast) {
                            drawLine(
                                color = primaryColor.copy(alpha = 0.5f),
                                start = Offset(x, paddingY / 2f),
                                end = Offset(x, height - paddingY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                            val endLabel = textMeasurer.measure(
                                text = "Конец вклада",
                                style = TextStyle(color = primaryColor, fontSize = 10.sp)
                            )
                            drawText(
                                textLayoutResult = endLabel,
                                topLeft = Offset(
                                    x = x - endLabel.size.width - 8.dp.toPx(),
                                    y = paddingY / 2f
                                )
                            )
                            
                            if (selectedIndex == null) {
                                val amountText = formatter.format(value).replace(",00", "").replace(",0", "")
                                val amountLabel = textMeasurer.measure(
                                    text = amountText,
                                    style = TextStyle(color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                )
                                drawText(
                                    textLayoutResult = amountLabel,
                                    topLeft = Offset(
                                        x = x - amountLabel.size.width,
                                        y = y - amountLabel.size.height - 12.dp.toPx()
                                    )
                                )
                            }
                        }
                    }
                    
                    selectedIndex?.let { index ->
                        val value = data[index]
                        val x = index * stepX
                        val normalizedY = if (range == 0f) 0.5f else (value.toFloat() - minValue) / range
                        val y = height - paddingY - (normalizedY * chartHeight)
                        
                        drawLine(
                            color = textColor,
                            start = Offset(x, paddingY / 2f),
                            end = Offset(x, height - paddingY),
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        drawCircle(
                            color = primaryColor,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        
                        val termText = if (isTermInYears) "$index год" else "$index месяц"
                        val amountText = formatter.format(value).replace(",00", "").replace(",0", "")
                        
                        val termLabel = textMeasurer.measure(
                            text = termText,
                            style = TextStyle(color = textColor, fontSize = 10.sp)
                        )
                        val amountLabel = textMeasurer.measure(
                            text = amountText,
                            style = TextStyle(color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        )
                        
                        val tooltipWidth = maxOf(termLabel.size.width, amountLabel.size.width) + 32.dp.toPx()
                        val tooltipHeight = termLabel.size.height + amountLabel.size.height + 24.dp.toPx()
                        
                        var tooltipX = x - tooltipWidth / 2f
                        if (tooltipX < 0f) tooltipX = 0f
                        if (tooltipX + tooltipWidth > width) tooltipX = width - tooltipWidth
                        
                        val tooltipY = 0f 
                        
                        drawRoundRect(
                            color = androidx.compose.ui.graphics.Color(0xFF2B2930), // SurfaceDark
                            topLeft = Offset(tooltipX, tooltipY),
                            size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                        )
                        
                        drawText(
                            textLayoutResult = termLabel,
                            topLeft = Offset(
                                x = tooltipX + 16.dp.toPx(),
                                y = tooltipY + 8.dp.toPx()
                            )
                        )
                        
                        drawText(
                            textLayoutResult = amountLabel,
                            topLeft = Offset(
                                x = tooltipX + 16.dp.toPx(),
                                y = tooltipY + 8.dp.toPx() + termLabel.size.height
                            )
                        )
                    }
                }
            }
        }
    }
}