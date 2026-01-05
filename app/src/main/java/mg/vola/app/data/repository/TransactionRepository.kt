package com.vola.app.data.repository

import com.vola.app.data.local.database.dao.TransactionDao
import com.vola.app.data.models.Transaction
import com.vola.app.data.models.TransactionFilters
import com.vola.app.data.models.TransactionSummary
import com.vola.app.domain.constants.Categories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }
    
    suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)
    }
    
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
    
    suspend fun deleteTransactionById(id: String) {
        transactionDao.deleteTransactionById(id)
    }
    
    fun getTransactionsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }
    
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category)
    }
    
    fun getTransactionsByFilters(filters: TransactionFilters): Flow<List<Transaction>> {
        // Implementation with dynamic query builder
        return getAllTransactions().map { transactions ->
            transactions.filter { transaction ->
                var matches = true
                
                filters.startDate?.let {
                    matches = matches && transaction.date >= it
                }
                
                filters.endDate?.let {
                    matches = matches && transaction.date <= it
                }
                
                filters.category?.let {
                    matches = matches && transaction.category == it
                }
                
                filters.type?.let {
                    matches = matches && transaction.type == it
                }
                
                filters.accountId?.let {
                    matches = matches && transaction.accountId == it
                }
                
                filters.minAmount?.let {
                    matches = matches && transaction.amount >= it
                }
                
                filters.maxAmount?.let {
                    matches = matches && transaction.amount <= it
                }
                
                matches
            }
        }
    }
    
    suspend fun getTransactionSummary(
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionSummary {
        return transactionDao.getTransactionSummary(startDate, endDate)
            ?: TransactionSummary()
    }
    
    suspend fun getCategorySummary(
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<String, Double> {
        val summaries = transactionDao.getCategorySummary(startDate, endDate)
        return summaries.groupBy { it.category }
            .mapValues { (_, summaries) ->
                summaries.sumOf { it.amount }
            }
    }
    
    suspend fun getRecentTransactions(limit: Int = 5): List<Transaction> {
        return getAllTransactions().map { transactions ->
            transactions.sortedByDescending { it.date }
                .take(limit)
        }.firstOrNull() ?: emptyList()
    }
    
    suspend fun getMonthlySummary(year: Int, month: Int): TransactionSummary {
        val startDate = LocalDate(year, month, 1)
        val endDate = if (month == 12) {
            LocalDate(year + 1, 1, 1).minusDays(1)
        } else {
            LocalDate(year, month + 1, 1).minusDays(1)
        }
        
        return getTransactionSummary(startDate, endDate)
    }
    
    suspend fun getTodayTransactions(): List<Transaction> {
        val today = LocalDateTime.now().date
        return getTransactionsByDateRange(today, today)
            .firstOrNull() ?: emptyList()
    }
    
    suspend fun getThisMonthTransactions(): List<Transaction> {
        val now = LocalDateTime.now()
        val startDate = LocalDate(now.year, now.monthNumber, 1)
        val endDate = if (now.monthNumber == 12) {
            LocalDate(now.year + 1, 1, 1).minusDays(1)
        } else {
            LocalDate(now.year, now.monthNumber + 1, 1).minusDays(1)
        }
        
        return getTransactionsByDateRange(startDate, endDate)
            .firstOrNull() ?: emptyList()
    }
}