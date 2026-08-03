package com.example

data class CalculationResult(
    val finalAmount: Double,
    val profit: Double,
    val growthData: List<Double>
)

class DepositCalculator {
    fun calculate(
        initialAmount: Double,
        termValue: Double,
        rate: Double,
        isTermInYears: Boolean,
        isCapitalization: Boolean
    ): CalculationResult {
        if (initialAmount <= 0 || termValue <= 0 || rate <= 0) {
            return CalculationResult(0.0, 0.0, emptyList())
        }

        val totalMonths = if (isTermInYears) (termValue * 12).toInt() else termValue.toInt()
        val growth = mutableListOf<Double>()
        growth.add(initialAmount)

        var currentAmount = initialAmount
        if (isCapitalization) {
            for (i in 1..totalMonths) {
                currentAmount *= (1 + (rate / 100) / 12)
                growth.add(currentAmount)
            }
        } else {
            val monthlyProfit = initialAmount * (rate / 100) / 12.0
            for (i in 1..totalMonths) {
                currentAmount += monthlyProfit
                growth.add(currentAmount)
            }
        }
        
        val finalAmount = currentAmount
        val profit = finalAmount - initialAmount
        
        return CalculationResult(finalAmount, profit, growth)
    }
}
