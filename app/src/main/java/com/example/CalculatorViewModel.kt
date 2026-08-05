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
    val growthData: List<ChartPoint> = emptyList(),
    val amountError: Boolean = false,
    val termError: Boolean = false,
    val rateError: Boolean = false
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
        calculate()
    }

    fun onTermChanged(value: String) {
        _state.value = _state.value.copy(term = value)
        calculate()
    }

    fun onTermUnitChanged(unit: TermUnit) {
        _state.value = _state.value.copy(termUnit = unit)
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
    
    fun onStartDateChanged(date: LocalDate) {
        _state.value = _state.value.copy(startDate = date)
        calculate()
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

    fun onDeleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    private fun calculate() {
        val s = _state.value
        val amount = s.initialAmount.replace(',', '.').toDoubleOrNull()
        val termValue = s.term.replace(',', '.').toDoubleOrNull()
        val rate = s.interestRate.replace(',', '.').toDoubleOrNull()

        val amountError = amount == null || amount <= 0
        val termError = termValue == null || termValue <= 0
        val rateError = rate == null || rate < 0

        if (amountError || termError || rateError) {
            _state.value = _state.value.copy(
                finalAmount = 0.0,
                profit = 0.0,
                calculatedEffectiveRate = null,
                growthData = emptyList(),
                amountError = amountError,
                termError = termError,
                rateError = rateError
            )
            return
        }

        val safeAmount = amount!!
        val safeTermValue = termValue!!
        val safeRate = rate!!

        val result = calculator.calculate(safeAmount, safeTermValue, safeRate, s.termUnit, s.isCapitalization, s.isEffectiveRate, s.startDate)

        val endDate = when (s.termUnit) {
            TermUnit.DAYS -> s.startDate.plusDays(safeTermValue.toLong())
            TermUnit.MONTHS -> s.startDate.plusMonths(safeTermValue.toLong())
            TermUnit.YEARS -> s.startDate.plusYears(safeTermValue.toLong())
        }
        val daysInTerm = java.time.temporal.ChronoUnit.DAYS.between(s.startDate, endDate)

        val calculatedEffectiveRate = if (!s.isEffectiveRate && s.isCapitalization && safeRate > 0 && daysInTerm > 0 && safeAmount > 0) {
            (result.profit / safeAmount) * (365.0 / daysInTerm) * 100.0
        } else null

        _state.value = _state.value.copy(
            finalAmount = result.finalAmount,
            profit = result.profit,
            calculatedEffectiveRate = calculatedEffectiveRate,
            growthData = result.growthData,
            amountError = amountError,
            termError = termError,
            rateError = rateError
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

