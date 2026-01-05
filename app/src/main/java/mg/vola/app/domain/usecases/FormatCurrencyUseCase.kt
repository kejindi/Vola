package com.vola.app.domain.usecases

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class FormatCurrencyUseCase @Inject constructor() {
    
    private val mgaLocale = Locale("mg", "MG")
    private val mgaCurrency = Currency.getInstance("MGA")
    
    operator fun invoke(amount: Double, currencyCode: String = "MGA"): String {
        return when (currencyCode.uppercase()) {
            "MGA" -> formatMGA(amount)
            "USD" -> formatUSD(amount)
            "EUR" -> formatEUR(amount)
            else -> formatGeneric(amount, currencyCode)
        }
    }
    
    private fun formatMGA(amount: Double): String {
        return if (amount >= 1000000) {
            "${formatNumber(amount / 1000000)}M MGA"
        } else if (amount >= 1000) {
            "${formatNumber(amount / 1000)}K MGA"
        } else {
            "${formatNumber(amount)} MGA"
        }
    }
    
    private fun formatUSD(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        return formatter.format(amount)
    }
    
    private fun formatEUR(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
        formatter.currency = Currency.getInstance("EUR")
        return formatter.format(amount)
    }
    
    private fun formatGeneric(amount: Double, currencyCode: String): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        try {
            formatter.currency = Currency.getInstance(currencyCode)
        } catch (e: IllegalArgumentException) {
            // If currency code is invalid, fall back to number format
            return "${formatNumber(amount)} $currencyCode"
        }
        return formatter.format(amount)
    }
    
    private fun formatNumber(number: Double): String {
        return if (number % 1 == 0.0) {
            String.format("%.0f", number)
        } else {
            String.format("%.2f", number)
        }
    }
    
    fun parseCurrency(amountString: String): Double {
        return try {
            amountString.replace("[^\\d.]".toRegex(), "").toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }
    
    fun formatForInput(amount: Double): String {
        return String.format("%.0f", amount)
    }
    
    fun formatWithSymbol(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(mgaLocale)
        formatter.maximumFractionDigits = 0
        return "${formatter.format(amount)} ${mgaCurrency.symbol}"
    }
    
    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> "${formatNumber(amount / 1_000_000_000)}B"
            amount >= 1_000_000 -> "${formatNumber(amount / 1_000_000)}M"
            amount >= 1_000 -> "${formatNumber(amount / 1_000)}K"
            else -> formatNumber(amount)
        }
    }
    
    fun calculatePercentageChange(oldAmount: Double, newAmount: Double): String {
        if (oldAmount == 0.0) return "0%"
        val percentage = ((newAmount - oldAmount) / oldAmount) * 100
        val sign = if (percentage >= 0) "+" else ""
        return "$sign${String.format("%.1f", percentage)}%"
    }
    
    fun calculateDifference(oldAmount: Double, newAmount: Double): String {
        val difference = newAmount - oldAmount
        val sign = if (difference >= 0) "+" else ""
        return "$sign${formatMGA(difference)}"
    }
}