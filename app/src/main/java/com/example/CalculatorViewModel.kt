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
import java.time.LocalDate

data class CalculatorState(
    val initialAmount: String = "100000",
    val term: String = "12",
    val termUnit: TermUnit = TermUnit.MONTHS,
    val interestRate: String = "15.0",
    val isEffectiveRate: Boolean = false,
    val isCapitalization: Boolean = false,
    val finalAmount: Double = 0.0,
    val profit: Double = 0.0,
    val calculatedEffectiveRate: Double? = null,
    val startDate: LocalDate = LocalDate.now(),
    val growthData: List<ChartPoint> = emptyList()
)

class CalculatorViewModel(private val repository: HistoryRepository) : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    val history = repository.allHistory


    private val calculator = DepositCalculator()

    init {
        calculate()
    }

    fun onInitialAmountChanged(value: String) {
        _state.value = _state.value.copy(initialAmount = value)
    }

    fun onTermChanged(value: String) {
        _state.value = _state.value.copy(term = value)
    }

    fun onTermUnitChanged(unit: TermUnit) {
        _state.value = _state.value.copy(termUnit = unit)
    }

    fun onInterestRateChanged(value: String) {
        _state.value = _state.value.copy(interestRate = value)
    }

    fun onRateTypeChanged(isEffective: Boolean) {
        _state.value = _state.value.copy(isEffectiveRate = isEffective)
    }

    fun onCapitalizationChanged(value: Boolean) {
        _state.value = _state.value.copy(isCapitalization = value)
    }
    
    fun onStartDateChanged(date: LocalDate) {
        _state.value = _state.value.copy(startDate = date)
    }

    fun onCalculateClicked() {
        calculate()
    }

    fun onSaveClicked() {
        val s = _state.value
        if (s.finalAmount > 0) {
            viewModelScope.launch {
                val item = HistoryItem(
                    initialAmount = s.initialAmount,
                    term = s.term,
                    termUnitName = s.termUnit.name,
                    interestRate = s.interestRate,
                    isEffectiveRate = s.isEffectiveRate,
                    isCapitalization = s.isCapitalization,
                    finalAmount = s.finalAmount,
                    profit = s.profit,
                    calculatedEffectiveRate = s.calculatedEffectiveRate,
                    startDate = s.startDate.toEpochDay()
                )
                repository.insert(item)
            }
        }
    }

    private fun calculate() {
        val s = _state.value
        val amount = s.initialAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
        val termValue = s.term.replace(',', '.').toDoubleOrNull() ?: 0.0
        val rate = s.interestRate.replace(',', '.').toDoubleOrNull() ?: 0.0

        val result = calculator.calculate(amount, termValue, rate, s.termUnit, s.isCapitalization, s.isEffectiveRate, s.startDate)

        val calculatedEffectiveRate = if (!s.isEffectiveRate && s.isCapitalization && rate > 0 && termValue > 0 && amount > 0) {
            val termInYears = when (s.termUnit) {
                TermUnit.YEARS -> termValue
                TermUnit.MONTHS -> termValue / 12.0
                TermUnit.DAYS -> termValue / 365.0 // Approximate for effective rate calculation display
            }
            if (termInYears > 0) {
                val growthOverTerm = result.profit / amount
                (growthOverTerm / termInYears) * 100
            } else null
        } else null

        _state.value = _state.value.copy(
            finalAmount = result.finalAmount,
            profit = result.profit,
            calculatedEffectiveRate = calculatedEffectiveRate,
            growthData = result.growthData
        )
    }
}

class CalculatorViewModelFactory(private val repository: HistoryRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

