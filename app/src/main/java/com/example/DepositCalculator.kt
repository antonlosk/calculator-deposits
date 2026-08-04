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
        startDate: LocalDate,
        payoutPeriod: PayoutPeriod
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
        
        var capPeriodsCount = 0L
        var nextCapDate = when (payoutPeriod) {
            PayoutPeriod.DAILY -> startDate.plusDays(1)
            PayoutPeriod.MONTHLY -> startDate.plusMonths(1)
            PayoutPeriod.QUARTERLY -> startDate.plusMonths(3)
            PayoutPeriod.ANNUALLY -> startDate.plusYears(1)
            PayoutPeriod.AT_END -> endDate
        }
        
        var monthIndex = 1
        var nextMonthDate = startDate.plusMonths(1)

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
                
                capPeriodsCount++
                nextCapDate = when (payoutPeriod) {
                    PayoutPeriod.DAILY -> startDate.plusDays(capPeriodsCount + 1)
                    PayoutPeriod.MONTHLY -> startDate.plusMonths(capPeriodsCount + 1)
                    PayoutPeriod.QUARTERLY -> startDate.plusMonths((capPeriodsCount + 1) * 3)
                    PayoutPeriod.ANNUALLY -> startDate.plusYears(capPeriodsCount + 1)
                    PayoutPeriod.AT_END -> endDate
                }
                if (nextCapDate.isAfter(endDate)) {
                    nextCapDate = endDate
                }
            }
            
            if (currentDate == nextMonthDate || currentDate == endDate) {
                val displayAmount = if (useCapitalization) currentBalance + accumulatedInterest else initialAmount + totalProfit
                growth.add(ChartPoint(monthIndex, displayAmount, totalProfit))
                monthIndex++
                nextMonthDate = startDate.plusMonths(monthIndex.toLong())
            }
        }
        
        val finalAmount = initialAmount + totalProfit
        
        return CalculationResult(finalAmount, totalProfit, growth)
    }
}
