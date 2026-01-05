package com.vola.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val accountId: String,
    val date: LocalDate,
    val time: LocalTime? = null,
    val merchant: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val receiptUri: String? = null,
    val isRecurring: Boolean = false,
    val recurringType: RecurringType? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isExpense: Boolean get() = type == TransactionType.EXPENSE
    val isIncome: Boolean get() = type == TransactionType.INCOME
}

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class RecurringType {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

data class TransactionSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netAmount: Double = 0.0,
    val count: Int = 0
)

data class TransactionFilters(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val category: String? = null,
    val type: TransactionType? = null,
    val accountId: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)