package com.vola.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val spent: Double = 0.0,
    val color: String = "#2E8B57",
    val icon: String = "💰",
    val notificationsEnabled: Boolean = true,
    val alertThreshold: Double = 0.8, // 80%
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BudgetPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

data class BudgetStatus(
    val budget: Budget,
    val spent: Double,
    val remaining: Double,
    val percentage: Double,
    val isOverBudget: Boolean = false,
    val daysRemaining: Int = 0
)

data class BudgetSummary(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val overallPercentage: Double = 0.0,
    val budgets: List<BudgetStatus> = emptyList()
)