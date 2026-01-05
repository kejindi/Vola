package com.vola.app.domain.usecases

import com.vola.app.data.models.Budget
import com.vola.app.data.models.BudgetStatus
import com.vola.app.data.models.BudgetSummary
import com.vola.app.data.models.Transaction
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class CalculateBudgetUseCase @Inject constructor() {
    
    operator fun invoke(
        budgets: List<Budget>,
        transactions: List<Transaction>,
        startDate: LocalDate,
        endDate: LocalDate
    ): BudgetSummary {
        // Filter transactions for the date range
        val filteredTransactions = transactions.filter { transaction ->
            transaction.date >= startDate && transaction.date <= endDate
        }
        
        // Calculate status for each budget
        val budgetStatuses = budgets.map { budget ->
            val spent = filteredTransactions
                .filter { it.category == budget.category }
                .sumOf { it.amount }
            
            val remaining = budget.amount - spent
            val percentage = (spent / budget.amount) * 100
            val isOverBudget = spent > budget.amount
            
            BudgetStatus(
                budget = budget,
                spent = spent,
                remaining = remaining,
                percentage = percentage,
                isOverBudget = isOverBudget,
                daysRemaining = 0 // Will be calculated separately
            )
        }
        
        // Calculate totals
        val totalBudget = budgets.sumOf { it.amount }
        val totalSpent = budgetStatuses.sumOf { it.spent }
        val totalRemaining = totalBudget - totalSpent
        val overallPercentage = if (totalBudget > 0) (totalSpent / totalBudget) * 100 else 0.0
        
        return BudgetSummary(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            totalRemaining = totalRemaining,
            overallPercentage = overallPercentage,
            budgets = budgetStatuses
        )
    }
    
    fun calculateRemainingDays(budget: Budget, currentDate: LocalDate): Int {
        return budget.endDate.toEpochDays() - currentDate.toEpochDays()
    }
    
    fun calculateDailyBudget(budget: Budget, remainingDays: Int): Double {
        return if (remainingDays > 0) budget.remaining / remainingDays else 0.0
    }
    
    fun isBudgetAlertNeeded(budgetStatus: BudgetStatus): Boolean {
        return budgetStatus.percentage >= 80 && !budgetStatus.isOverBudget
    }
    
    fun calculateSavingsPotential(
        budgets: List<Budget>,
        transactions: List<Transaction>,
        month: LocalDate
    ): Double {
        val monthStart = LocalDate(month.year, month.monthNumber, 1)
        val monthEnd = if (month.monthNumber == 12) {
            LocalDate(month.year + 1, 1, 1).minusDays(1)
        } else {
            LocalDate(month.year, month.monthNumber + 1, 1).minusDays(1)
        }
        
        val summary = invoke(budgets, transactions, monthStart, monthEnd)
        return max(0.0, summary.totalRemaining)
    }
    
    fun suggestBudgetAdjustment(
        currentBudget: Budget,
        historicalSpending: Double,
        suggestedIncrease: Double = 0.1
    ): Budget {
        val adjustmentFactor = if (historicalSpending > currentBudget.amount) {
            1.0 + suggestedIncrease
        } else if (historicalSpending < currentBudget.amount * 0.7) {
            0.9 // Reduce by 10%
        } else {
            1.0 // No change
        }
        
        return currentBudget.copy(
            amount = currentBudget.amount * adjustmentFactor
        )
    }
}