package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlin.math.pow

data class CalculatorState(
    val initialAmount: String = "100000",
    val term: String = "12",
    val isTermInYears: Boolean = false,
    val interestRate: String = "15.0",
    val isCapitalization: Boolean = false,
    val finalAmount: Double = 0.0,
    val profit: Double = 0.0,
    val growthData: List<Double> = emptyList()
)

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    init {
        calculate()
    }

    fun onInitialAmountChanged(value: String) {
        _state.value = _state.value.copy(initialAmount = value)
        calculate()
    }

    fun onTermChanged(value: String) {
        _state.value = _state.value.copy(term = value)
        calculate()
    }

    fun onTermTypeChanged(isYears: Boolean) {
        _state.value = _state.value.copy(isTermInYears = isYears)
        calculate()
    }

    fun onInterestRateChanged(value: String) {
        _state.value = _state.value.copy(interestRate = value)
        calculate()
    }

    fun onCapitalizationChanged(value: Boolean) {
        _state.value = _state.value.copy(isCapitalization = value)
        calculate()
    }

    private fun calculate() {
        val s = _state.value
        val amount = s.initialAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
        val termValue = s.term.replace(',', '.').toDoubleOrNull() ?: 0.0
        val rate = s.interestRate.replace(',', '.').toDoubleOrNull() ?: 0.0

        if (amount <= 0 || termValue <= 0 || rate <= 0) {
            _state.value = _state.value.copy(finalAmount = 0.0, profit = 0.0, growthData = emptyList())
            return
        }

        val totalMonths = if (s.isTermInYears) (termValue * 12).toInt() else termValue.toInt()
        val growth = mutableListOf<Double>()
        growth.add(amount)

        var currentAmount = amount
        if (s.isCapitalization) {
            for (i in 1..totalMonths) {
                currentAmount *= (1 + (rate / 100) / 12)
                growth.add(currentAmount)
            }
        } else {
            val monthlyProfit = amount * (rate / 100) / 12.0
            for (i in 1..totalMonths) {
                currentAmount += monthlyProfit
                growth.add(currentAmount)
            }
        }
        val finalAmount = currentAmount

        _state.value = _state.value.copy(
            finalAmount = finalAmount,
            profit = finalAmount - amount,
            growthData = growth
        )
    }
}
