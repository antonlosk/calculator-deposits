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
    val months: String = "12",
    val interestRate: String = "15.0",
    val isCapitalization: Boolean = false,
    val finalAmount: Double = 0.0,
    val profit: Double = 0.0,
    val aiResponse: String = "",
    val isLoadingAi: Boolean = false
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

    fun onMonthsChanged(value: String) {
        _state.value = _state.value.copy(months = value)
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
        val amount = s.initialAmount.toDoubleOrNull() ?: 0.0
        val months = s.months.toIntOrNull() ?: 0
        val rate = s.interestRate.toDoubleOrNull() ?: 0.0

        if (amount <= 0 || months <= 0 || rate <= 0) {
            _state.value = _state.value.copy(finalAmount = 0.0, profit = 0.0)
            return
        }

        val finalAmount = if (s.isCapitalization) {
            // A = P(1 + r/n)^(nt). For monthly, n=12, t=months/12 -> nt = months
            // So A = P(1 + (rate/100)/12)^months
            amount * (1 + (rate / 100) / 12).pow(months)
        } else {
            // Simple interest for the term
            amount + (amount * (rate / 100) * (months / 12.0))
        }

        _state.value = _state.value.copy(
            finalAmount = finalAmount,
            profit = finalAmount - amount
        )
    }

    fun askAiAboutRates() {
        _state.value = _state.value.copy(isLoadingAi = true, aiResponse = "")
        viewModelScope.launch {
            val response = askGemini(
                prompt = "Найди и расскажи про актуальные максимальные процентные ставки по банковским вкладам в России на данный момент. Кратко и по делу.",
                model = "gemini-3.5-flash",
                useSearch = true
            )
            _state.value = _state.value.copy(isLoadingAi = false, aiResponse = response)
        }
    }

    fun askAiAdvice(query: String) {
        if (query.isBlank()) return
        _state.value = _state.value.copy(isLoadingAi = true, aiResponse = "")
        viewModelScope.launch {
            val s = _state.value
            val context = "Пользователь рассчитывает вклад: сумма ${s.initialAmount} руб., срок ${s.months} мес., ставка ${s.interestRate}%. Капитализация: ${if(s.isCapitalization) "Да" else "Нет"}."
            val response = askGemini(
                prompt = "$context Вопрос: $query",
                model = "gemini-3.1-pro-preview",
                useHighThinking = true
            )
            _state.value = _state.value.copy(isLoadingAi = false, aiResponse = response)
        }
    }

    private suspend fun askGemini(prompt: String, model: String, useSearch: Boolean = false, useHighThinking: Boolean = false): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    return@withContext "Ошибка: API ключ Gemini не настроен. Добавьте его в Secrets AI Studio."
                }

                val tools = if (useSearch) listOf(Tool(googleSearch = JsonObject(emptyMap()))) else null
                val config = if (useHighThinking) GenerationConfig(thinkingConfig = ThinkingConfig("HIGH")) else null

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    tools = tools,
                    generationConfig = config
                )

                val url = "v1beta/models/$model:generateContent"
                val response = RetrofitClient.service.generateContent(url, apiKey, request)
                
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Пустой ответ от ИИ."
            } catch (e: Exception) {
                "Ошибка сети или API: ${e.message}"
            }
        }
    }
}
