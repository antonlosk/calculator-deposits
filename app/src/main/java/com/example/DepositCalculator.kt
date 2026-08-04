package com.example

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class TermUnit {
    DAYS, MONTHS, YEARS
}

data class ChartPoint(
    val monthsFromStart: Int, // Can be days or months, but let's keep it abstract, maybe rename to 'unitsFromStart'
    val amount: Double,
    val profit: Double
)

data class CalculationResult(
    val finalAmount: Double,
    val profit: Double,
    val growthData: List<ChartPoint>
)

class DepositCalculator {
    fun calculate(
        initialAmount: Double,
        termValue: Double,
        rate: Double,
        termUnit: TermUnit,
        isCapitalization: Boolean,
        isEffectiveRate: Boolean,
        startDate: LocalDate
    ): CalculationResult {
        if (initialAmount <= 0 || termValue <= 0 || rate <= 0) {
            return CalculationResult(0.0, 0.0, emptyList())
        }

        val endDate = when (termUnit) {
            TermUnit.DAYS -> startDate.plusDays(termValue.toLong())
            TermUnit.MONTHS -> startDate.plusMonths(termValue.toLong())
            TermUnit.YEARS -> startDate.plusYears(termValue.toLong())
        }

        val growth = mutableListOf<ChartPoint>()
        growth.add(ChartPoint(0, initialAmount, 0.0))

        val useCapitalization = isCapitalization && !isEffectiveRate
        var currentBalance = initialAmount
        var accumulatedInterest = 0.0
        var totalProfit = 0.0
        
        var currentDate = startDate
        
        var monthIndex = 1
        var nextCapDate = startDate.plusMonths(1)

        while (currentDate.isBefore(endDate)) {
            val daysInYear = if (currentDate.isLeapYear) 366 else 365
            val dailyInterest = currentBalance * (rate / 100.0) / daysInYear
            
            accumulatedInterest += dailyInterest
            
            currentDate = currentDate.plusDays(1)
            
            if (currentDate == nextCapDate || currentDate == endDate) {
                val roundedInterest = kotlin.math.round(accumulatedInterest * 100) / 100.0
                totalProfit += roundedInterest

                if (useCapitalization) {
                    currentBalance += roundedInterest
                }
                accumulatedInterest = 0.0
                
                val displayAmount = if (useCapitalization) currentBalance else initialAmount + totalProfit
                growth.add(ChartPoint(monthIndex, displayAmount, totalProfit))
                
                if (currentDate == nextCapDate) {
                    monthIndex++
                    nextCapDate = startDate.plusMonths(monthIndex.toLong())
                }
            }
        }
        
        val finalAmount = initialAmount + totalProfit
        
        return CalculationResult(finalAmount, totalProfit, growth)
    }
}
