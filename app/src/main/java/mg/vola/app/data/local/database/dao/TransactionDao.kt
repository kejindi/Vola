package com.vola.app.data.local.database.dao

import androidx.room.*
import com.vola.app.data.models.Transaction
import com.vola.app.data.models.TransactionSummary
import com.vola.app.data.models.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY date DESC, created_at DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): Transaction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)
    
    // Filtered queries
    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC, created_at DESC
    """)
    fun getTransactionsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        AND category = :category
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRangeAndCategory(
        startDate: LocalDate,
        endDate: LocalDate,
        category: String
    ): Flow<List<Transaction>>
    
    // Summary queries
    @Query("""
        SELECT 
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense,
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) as netAmount,
            COUNT(*) as count
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTransactionSummary(
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionSummary?
    
    @Query("""
        SELECT 
            category,
            SUM(amount) as amount,
            type
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY category, type
        ORDER BY amount DESC
    """)
    suspend fun getCategorySummary(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CategorySummary>
    
    data class CategorySummary(
        val category: String,
        val amount: Double,
        val type: TransactionType
    )
}