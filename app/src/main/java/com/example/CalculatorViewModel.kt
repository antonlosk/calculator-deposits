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
    val isEffectiveRate: Boolean = false,
    val isCapitalization: Boolean = false,
    val finalAmount: Double = 0.0,
    val profit: Double = 0.0,
    val calculatedEffectiveRate: Double? = null,
    val growthData: List<ChartPoint> = emptyList()
)

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val calculator = DepositCalculator()

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

    fun onRateTypeChanged(isEffective: Boolean) {
        _state.value = _state.value.copy(isEffectiveRate = isEffective)
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

        val result = calculator.calculate(amount, termValue, rate, s.isTermInYears, s.isCapitalization, s.isEffectiveRate)

        val calculatedEffectiveRate = if (!s.isEffectiveRate && s.isCapitalization && rate > 0) {
            ((1 + rate / 100 / 12).pow(12) - 1) * 100
        } else null

        _state.value = _state.value.copy(
            finalAmount = result.finalAmount,
            profit = result.profit,
            calculatedEffectiveRate = calculatedEffectiveRate,
            growthData = result.growthData
        )
    }
}
