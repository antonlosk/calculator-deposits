package com.example

import java.time.LocalDate

data class ChartPoint(
    val monthsFromStart: Int,
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
        isTermInYears: Boolean,
        isCapitalization: Boolean,
        isEffectiveRate: Boolean,
        startDate: LocalDate
    ): CalculationResult {
        if (initialAmount <= 0 || termValue <= 0 || rate <= 0) {
            return CalculationResult(0.0, 0.0, emptyList())
        }

        val totalMonths = if (isTermInYears) (termValue * 12).toInt() else termValue.toInt()
        val growth = mutableListOf<ChartPoint>()
        growth.add(ChartPoint(0, initialAmount, 0.0))

        val useCapitalization = isCapitalization && !isEffectiveRate
        var currentBalance = initialAmount
        var accumulatedInterest = 0.0
        var totalProfit = 0.0
        
        var currentDate = startDate
        val endDate = startDate.plusMonths(totalMonths.toLong())
        
        var monthIndex = 1
        var nextCapDate = startDate.plusMonths(1)

        while (currentDate.isBefore(endDate)) {
            val daysInYear = if (currentDate.isLeapYear) 366 else 365
            val dailyInterest = currentBalance * (rate / 100.0) / daysInYear
            
            accumulatedInterest += dailyInterest
            totalProfit += dailyInterest
            
            currentDate = currentDate.plusDays(1)
            
            if (currentDate == nextCapDate) {
                if (useCapitalization) {
                    currentBalance += accumulatedInterest
                    accumulatedInterest = 0.0
                }
                
                val displayAmount = if (useCapitalization) currentBalance else initialAmount + totalProfit
                growth.add(ChartPoint(monthIndex, displayAmount, totalProfit))
                
                monthIndex++
                nextCapDate = startDate.plusMonths(monthIndex.toLong())
            }
        }
        
        val finalAmount = initialAmount + totalProfit
        
        return CalculationResult(finalAmount, totalProfit, growth)
    }
}
