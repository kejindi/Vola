package com.vola.app.data.local.database.dao

import androidx.room.*
import com.vola.app.data.models.Budget
import com.vola.app.data.models.BudgetPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface BudgetDao {
    
    @Query("SELECT * FROM budgets ORDER BY created_at DESC")
    fun getAllBudgets(): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: String): Budget?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)
    
    @Update
    suspend fun updateBudget(budget: Budget)
    
    @Delete
    suspend fun deleteBudget(budget: Budget)
    
    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: String)
    
    @Query("SELECT * FROM budgets WHERE period = :period ORDER BY created_at DESC")
    fun getBudgetsByPeriod(period: BudgetPeriod): Flow<List<Budget>>
    
    @Query("""
        SELECT * FROM budgets 
        WHERE start_date <= :date AND end_date >= :date 
        ORDER BY created_at DESC
    """)
    fun getActiveBudgets(date: LocalDate): Flow<List<Budget>>
    
    @Query("""
        SELECT * FROM budgets 
        WHERE start_date BETWEEN :startDate AND :endDate 
        OR end_date BETWEEN :startDate AND :endDate
        ORDER BY start_date ASC
    """)
    fun getBudgetsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE category = :category ORDER BY created_at DESC")
    fun getBudgetsByCategory(category: String): Flow<List<Budget>>
    
    @Query("""
        SELECT 
            SUM(amount) as total_budget,
            SUM(spent) as total_spent,
            COUNT(*) as budget_count
        FROM budgets 
        WHERE start_date <= :date AND end_date >= :date
    """)
    suspend fun getBudgetSummary(date: LocalDate): BudgetSummary?
    
    @Query("""
        SELECT * FROM budgets 
        WHERE notifications_enabled = 1 
        AND (spent / amount) >= alert_threshold
        AND spent < amount
    """)
    fun getAlertingBudgets(): Flow<List<Budget>>
    
    @Query("SELECT COUNT(*) FROM budgets WHERE category = :category")
    suspend fun getBudgetCountByCategory(category: String): Int
    
    data class BudgetSummary(
        val total_budget: Double = 0.0,
        val total_spent: Double = 0.0,
        val budget_count: Int = 0
    )
}