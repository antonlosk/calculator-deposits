package com.example

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
        isEffectiveRate: Boolean
    ): CalculationResult {
        if (initialAmount <= 0 || termValue <= 0 || rate <= 0) {
            return CalculationResult(0.0, 0.0, emptyList())
        }

        val totalMonths = if (isTermInYears) (termValue * 12).toInt() else termValue.toInt()
        val growth = mutableListOf<ChartPoint>()
        growth.add(ChartPoint(0, initialAmount, 0.0))

        val useCapitalization = isCapitalization && !isEffectiveRate
        var currentAmount = initialAmount

        if (useCapitalization) {
            for (i in 1..totalMonths) {
                currentAmount *= (1 + (rate / 100) / 12)
                growth.add(ChartPoint(i, currentAmount, currentAmount - initialAmount))
            }
        } else {
            val monthlyProfit = initialAmount * (rate / 100) / 12.0
            for (i in 1..totalMonths) {
                currentAmount += monthlyProfit
                growth.add(ChartPoint(i, currentAmount, currentAmount - initialAmount))
            }
        }
        
        val finalAmount = currentAmount
        val profit = finalAmount - initialAmount
        
        return CalculationResult(finalAmount, profit, growth)
    }
}
