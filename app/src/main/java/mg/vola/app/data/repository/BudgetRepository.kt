package com.vola.app.data.repository

import com.vola.app.data.local.database.dao.BudgetDao
import com.vola.app.data.models.Budget
import com.vola.app.data.models.BudgetPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    
    fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets()
    }
    
    suspend fun getBudgetById(id: String): Budget? {
        return budgetDao.getBudgetById(id)
    }
    
    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }
    
    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }
    
    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }
    
    suspend fun deleteBudgetById(id: String) {
        budgetDao.deleteBudgetById(id)
    }
    
    fun getBudgetsByPeriod(period: BudgetPeriod): Flow<List<Budget>> {
        return budgetDao.getBudgetsByPeriod(period)
    }
    
    fun getBudgetsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Budget>> {
        return budgetDao.getBudgetsByDateRange(startDate, endDate)
    }
    
    fun getBudgetsByCategory(category: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsByCategory(category)
    }
    
    suspend fun getActiveBudgets(): List<Budget> {
        val now = LocalDate.now()
        return budgetDao.getActiveBudgets(now).firstOrNull() ?: emptyList()
    }
    
    suspend fun getBudgetSummary(startDate: LocalDate, endDate: LocalDate): Map<String, Any> {
        val budgets = budgetDao.getBudgetsByDateRange(startDate, endDate).firstOrNull() ?: emptyList()
        
        val totalBudget = budgets.sumOf { it.amount }
        val totalSpent = budgets.sumOf { it.spent }
        val totalRemaining = totalBudget - totalSpent
        val overallPercentage = if (totalBudget > 0) (totalSpent / totalBudget) * 100 else 0.0
        
        return mapOf(
            "totalBudget" to totalBudget,
            "totalSpent" to totalSpent,
            "totalRemaining" to totalRemaining,
            "overallPercentage" to overallPercentage,
            "budgetCount" to budgets.size
        )
    }
    
    suspend fun updateBudgetSpent(budgetId: String, spent: Double) {
        val budget = getBudgetById(budgetId) ?: return
        val updatedBudget = budget.copy(spent = spent)
        updateBudget(updatedBudget)
    }
    
    suspend fun incrementBudgetSpent(budgetId: String, amount: Double) {
        val budget = getBudgetById(budgetId) ?: return
        val updatedBudget = budget.copy(spent = budget.spent + amount)
        updateBudget(updatedBudget)
    }
    
    suspend fun resetMonthlyBudgets() {
        val budgets = getAllBudgets().firstOrNull() ?: emptyList()
        val now = LocalDate.now()
        
        budgets.filter { it.period == BudgetPeriod.MONTHLY && now.dayOfMonth == 1 }.forEach { budget ->
            val updatedBudget = budget.copy(spent = 0.0)
            updateBudget(updatedBudget)
        }
    }
    
    suspend fun duplicateBudget(budget: Budget): Budget {
        val newBudget = budget.copy(
            id = "",
            name = "${budget.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        insertBudget(newBudget)
        return newBudget
    }
}