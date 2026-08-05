package com.example

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

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
        var currentBalance = BigDecimal.valueOf(initialAmount)
        var accumulatedInterest = BigDecimal.ZERO
        var totalProfit = BigDecimal.ZERO
        
        var currentDate = startDate
        
        var monthIndex = 1
        var nextCapDate = startDate.plusMonths(1)

        val rateDecimal = BigDecimal.valueOf(rate)
        val hundred = BigDecimal("100")

        while (currentDate.isBefore(endDate)) {
            val daysInYear = BigDecimal(if (currentDate.isLeapYear) 366 else 365)
            val dailyInterest = currentBalance
                .multiply(rateDecimal)
                .divide(hundred, 10, RoundingMode.HALF_UP)
                .divide(daysInYear, 10, RoundingMode.HALF_UP)
            
            accumulatedInterest = accumulatedInterest.add(dailyInterest)
            
            currentDate = currentDate.plusDays(1)
            
            if (currentDate == nextCapDate || currentDate == endDate) {
                val roundedInterest = accumulatedInterest.setScale(2, RoundingMode.HALF_UP)
                totalProfit = totalProfit.add(roundedInterest)

                if (useCapitalization) {
                    currentBalance = currentBalance.add(roundedInterest)
                }
                accumulatedInterest = BigDecimal.ZERO
                
                val displayAmount = if (useCapitalization) {
                    currentBalance
                } else {
                    BigDecimal.valueOf(initialAmount).add(totalProfit)
                }
                growth.add(ChartPoint(monthIndex, displayAmount.toDouble(), totalProfit.toDouble()))
                
                if (currentDate == nextCapDate) {
                    monthIndex++
                    nextCapDate = startDate.plusMonths(monthIndex.toLong())
                }
            }
        }
        
        val finalAmount = BigDecimal.valueOf(initialAmount).add(totalProfit)
        
        return CalculationResult(
            finalAmount.setScale(2, RoundingMode.HALF_UP).toDouble(), 
            totalProfit.setScale(2, RoundingMode.HALF_UP).toDouble(), 
            growth
        )
    }
}
