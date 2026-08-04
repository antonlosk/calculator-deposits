#!/bin/bash
sed -i '3i import androidx.compose.animation.animateColorAsState\nimport androidx.compose.material3.SwipeToDismissBox\nimport androidx.compose.material3.SwipeToDismissBoxValue\nimport androidx.compose.material3.rememberSwipeToDismissBoxState\nimport androidx.compose.material3.ExperimentalMaterial3Api' app/src/main/java/com/example/CalculatorUI.kt
