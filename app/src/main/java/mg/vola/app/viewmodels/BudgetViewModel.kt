package com.vola.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vola.app.data.models.Budget
import com.vola.app.data.models.BudgetPeriod
import com.vola.app.data.models.BudgetStatus
import com.vola.app.data.repository.BudgetRepository
import com.vola.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    data class BudgetState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val budgets: List<Budget> = emptyList(),
        val budgetStatuses: List<BudgetStatus> = emptyList(),
        val selectedBudget: Budget? = null,
        val showCreateDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val totalBudget: Double = 0.0,
        val totalSpent: Double = 0.0,
        val totalRemaining: Double = 0.0,
        val overallPercentage: Double = 0.0,
        val selectedPeriod: BudgetPeriod = BudgetPeriod.MONTHLY,
        val selectedMonth: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        val budgetSuggestions: List<Budget> = emptyList()
    )
    
    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()
    
    init {
        loadBudgets()
        observeTransactions()
    }
    
    private fun loadBudgets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val budgets = budgetRepository.getAllBudgets().first()
                calculateBudgetStatuses(budgets)
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load budgets"
                    )
                }
            }
        }
    }
    
    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                calculateBudgetStatuses(_state.value.budgets, transactions)
            }
        }
    }
    
    private fun calculateBudgetStatuses(budgets: List<Budget>, transactions: List<com.vola.app.data.models.Transaction>? = null) {
        viewModelScope.launch {
            val currentMonth = _state.value.selectedMonth
            val startDate = LocalDate(currentMonth.year, currentMonth.monthNumber, 1)
            val endDate = if (currentMonth.monthNumber == 12) {
                LocalDate(currentMonth.year + 1, 1, 1).minusDays(1)
            } else {
                LocalDate(currentMonth.year, currentMonth.monthNumber + 1, 1).minusDays(1)
            }
            
            val filteredTransactions = transactions ?: 
                transactionRepository.getTransactionsByDateRange(startDate, endDate).first()
            
            val statuses = budgets.map { budget ->
                val spent = filteredTransactions
                    .filter { it.category == budget.category && it.type == com.vola.app.data.models.TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                val remaining = budget.amount - spent
                val percentage = (spent / budget.amount) * 100
                val isOverBudget = spent > budget.amount
                val daysRemaining = (endDate.toEpochDays() - currentMonth.toEpochDays()).toInt()
                
                BudgetStatus(
                    budget = budget,
                    spent = spent,
                    remaining = remaining,
                    percentage = percentage,
                    isOverBudget = isOverBudget,
                    daysRemaining = daysRemaining
                )
            }
            
            val totalBudget = budgets.sumOf { it.amount }
            val totalSpent = statuses.sumOf { it.spent }
            val totalRemaining = totalBudget - totalSpent
            val overallPercentage = if (totalBudget > 0) (totalSpent / totalBudget) * 100 else 0.0
            
            _state.update { state ->
                state.copy(
                    isLoading = false,
                    budgets = budgets,
                    budgetStatuses = statuses,
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    totalRemaining = totalRemaining,
                    overallPercentage = overallPercentage
                )
            }
        }
    }
    
    fun setSelectedPeriod(period: BudgetPeriod) {
        _state.update { it.copy(selectedPeriod = period) }
        loadBudgets()
    }
    
    fun setSelectedMonth(month: LocalDate) {
        _state.update { it.copy(selectedMonth = month) }
        loadBudgets()
    }
    
    fun selectBudget(budget: Budget?) {
        _state.update { it.copy(selectedBudget = budget) }
    }
    
    fun showCreateDialog(show: Boolean) {
        _state.update { it.copy(showCreateDialog = show) }
    }
    
    fun showDeleteDialog(show: Boolean) {
        _state.update { it.copy(showDeleteDialog = show) }
    }
    
    suspend fun saveBudget(budget: Budget) {
        try {
            if (budget.id.isBlank()) {
                budgetRepository.insertBudget(budget)
            } else {
                budgetRepository.updateBudget(budget)
            }
            loadBudgets()
        } catch (e: Exception) {
            throw Exception("Failed to save budget: ${e.message}")
        }
    }
    
    suspend fun deleteBudget(budget: Budget) {
        try {
            budgetRepository.deleteBudget(budget)
            loadBudgets()
        } catch (e: Exception) {
            throw Exception("Failed to delete budget: ${e.message}")
        }
    }
    
    fun getBudgetByCategory(category: String): Budget? {
        return _state.value.budgets.find { it.category == category }
    }
    
    fun getBudgetStatus(category: String): BudgetStatus? {
        return _state.value.budgetStatuses.find { it.budget.category == category }
    }
    
    fun isOverBudget(category: String): Boolean {
        return getBudgetStatus(category)?.isOverBudget ?: false
    }
    
    fun getRemainingBudget(category: String): Double {
        return getBudgetStatus(category)?.remaining ?: 0.0
    }
    
    fun getBudgetPercentage(category: String): Double {
        return getBudgetStatus(category)?.percentage ?: 0.0
    }
    
    fun generateBudgetSuggestions() {
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val startDate = LocalDate(now.year, now.monthNumber, 1).minusMonths(3)
                val endDate = now.date
                
                val transactions = transactionRepository.getTransactionsByDateRange(startDate, endDate).first()
                val expenseByCategory = transactions
                    .filter { it.type == com.vola.app.data.models.TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.amount } / 3 } // Average over 3 months
                
                val suggestions = expenseByCategory.map { (category, avgAmount) ->
                    Budget(
                        name = category.replaceFirstChar { it.uppercase() },
                        category = category,
                        amount = avgAmount * 1.1, // 10% buffer
                        period = BudgetPeriod.MONTHLY,
                        startDate = now.date,
                        endDate = if (now.monthNumber == 12) {
                            LocalDate(now.year + 1, 1, 1).minusDays(1)
                        } else {
                            LocalDate(now.year, now.monthNumber + 1, 1).minusDays(1)
                        }
                    )
                }
                
                _state.update { it.copy(budgetSuggestions = suggestions) }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun getBudgetInsights(): List<String> {
        val insights = mutableListOf<String>()
        val state = _state.value
        
        if (state.overallPercentage > 90) {
            insights.add("You've used ${state.overallPercentage.toInt()}% of your total budget")
        }
        
        state.budgetStatuses.filter { it.isOverBudget }.forEach { status ->
            insights.add("${status.budget.name} budget exceeded by ${(status.percentage - 100).toInt()}%")
        }
        
        val almostExhausted = state.budgetStatuses.filter { 
            it.percentage > 80 && it.percentage <= 100 
        }
        almostExhausted.forEach { status ->
            insights.add("${status.budget.name} budget almost exhausted (${status.percentage.toInt()}%)")
        }
        
        val underBudget = state.budgetStatuses.filter { it.percentage < 50 }
        if (underBudget.isNotEmpty() && state.overallPercentage > 70) {
            insights.add("Consider reallocating funds from under-utilized categories")
        }
        
        return insights.take(3)
    }
}