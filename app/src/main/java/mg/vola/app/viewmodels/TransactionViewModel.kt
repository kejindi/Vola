package com.vola.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vola.app.data.models.Transaction
import com.vola.app.data.models.TransactionFilters
import com.vola.app.data.models.TransactionType
import com.vola.app.data.repository.TransactionRepository
import com.vola.app.domain.constants.Categories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    data class TransactionState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val transactions: List<Transaction> = emptyList(),
        val filteredTransactions: List<Transaction> = emptyList(),
        val filters: TransactionFilters = TransactionFilters(),
        val selectedTransaction: Transaction? = null,
        val showDeleteDialog: Boolean = false,
        val showFilterDialog: Boolean = false,
        val searchQuery: String = "",
        val selectedCategory: String? = null,
        val selectedType: TransactionType? = null,
        val selectedDateRange: Pair<LocalDate, LocalDate>? = null
    )
    
    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        transactions = transactions,
                        filteredTransactions = applyFilters(transactions, state.filters, state.searchQuery)
                    )
                }
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transactions"
                    )
                }
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _state.update { state ->
            state.copy(
                searchQuery = query,
                filteredTransactions = applyFilters(state.transactions, state.filters, query)
            )
        }
    }
    
    fun setCategoryFilter(category: String?) {
        _state.update { state ->
            state.copy(
                filters = state.filters.copy(category = category),
                filteredTransactions = applyFilters(
                    state.transactions, 
                    state.filters.copy(category = category), 
                    state.searchQuery
                )
            )
        }
    }
    
    fun setTypeFilter(type: TransactionType?) {
        _state.update { state ->
            state.copy(
                filters = state.filters.copy(type = type),
                filteredTransactions = applyFilters(
                    state.transactions, 
                    state.filters.copy(type = type), 
                    state.searchQuery
                )
            )
        }
    }
    
    fun setDateRangeFilter(startDate: LocalDate?, endDate: LocalDate?) {
        _state.update { state ->
            state.copy(
                filters = state.filters.copy(startDate = startDate, endDate = endDate),
                filteredTransactions = applyFilters(
                    state.transactions, 
                    state.filters.copy(startDate = startDate, endDate = endDate), 
                    state.searchQuery
                )
            )
        }
    }
    
    fun selectTransaction(transaction: Transaction?) {
        _state.update { it.copy(selectedTransaction = transaction) }
    }
    
    fun showDeleteDialog(show: Boolean) {
        _state.update { it.copy(showDeleteDialog = show) }
    }
    
    fun showFilterDialog(show: Boolean) {
        _state.update { it.copy(showFilterDialog = show) }
    }
    
    fun clearFilters() {
        _state.update { state ->
            state.copy(
                filters = TransactionFilters(),
                searchQuery = "",
                filteredTransactions = state.transactions
            )
        }
    }
    
    suspend fun saveTransaction(transaction: Transaction) {
        try {
            if (transaction.id.isBlank()) {
                transactionRepository.insertTransaction(transaction)
            } else {
                transactionRepository.updateTransaction(transaction)
            }
            loadTransactions()
        } catch (e: Exception) {
            throw Exception("Failed to save transaction: ${e.message}")
        }
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        try {
            transactionRepository.deleteTransaction(transaction)
            loadTransactions()
        } catch (e: Exception) {
            throw Exception("Failed to delete transaction: ${e.message}")
        }
    }
    
    fun getTransactionsByDate(date: LocalDate): List<Transaction> {
        return _state.value.transactions.filter { it.date == date }
    }
    
    fun getTotalAmountByCategory(category: String): Double {
        return _state.value.transactions
            .filter { it.category == category }
            .sumOf { it.amount }
    }
    
    fun getTodayTransactions(): List<Transaction> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return _state.value.transactions.filter { it.date == today }
    }
    
    private fun applyFilters(
        transactions: List<Transaction>,
        filters: TransactionFilters,
        searchQuery: String
    ): List<Transaction> {
        return transactions.filter { transaction ->
            // Date filter
            val matchesDate = filters.startDate?.let { transaction.date >= it } ?: true &&
                              filters.endDate?.let { transaction.date <= it } ?: true
            
            // Category filter
            val matchesCategory = filters.category?.let { transaction.category == it } ?: true
            
            // Type filter
            val matchesType = filters.type?.let { transaction.type == it } ?: true
            
            // Account filter
            val matchesAccount = filters.accountId?.let { transaction.accountId == it } ?: true
            
            // Amount filter
            val matchesMinAmount = filters.minAmount?.let { transaction.amount >= it } ?: true
            val matchesMaxAmount = filters.maxAmount?.let { transaction.amount <= it } ?: true
            
            // Search query
            val matchesSearch = searchQuery.isEmpty() ||
                    transaction.merchant?.contains(searchQuery, ignoreCase = true) == true ||
                    transaction.notes?.contains(searchQuery, ignoreCase = true) == true ||
                    transaction.category.contains(searchQuery, ignoreCase = true) ||
                    transaction.amount.toString().contains(searchQuery)
            
            matchesDate && matchesCategory && matchesType && matchesAccount &&
            matchesMinAmount && matchesMaxAmount && matchesSearch
        }.sortedByDescending { it.date }
    }
    
    fun getCategorySummary(): Map<String, Double> {
        return _state.value.transactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }
    
    fun getMonthlySummary(year: Int, month: Int): Map<String, Any> {
        val filtered = _state.value.transactions.filter {
            it.date.year == year && it.date.monthNumber == month
        }
        
        val totalIncome = filtered.filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = filtered.filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val netAmount = totalIncome - totalExpense
        
        return mapOf(
            "totalIncome" to totalIncome,
            "totalExpense" to totalExpense,
            "netAmount" to netAmount,
            "transactionCount" to filtered.size,
            "topCategory" to getTopCategory(filtered)
        )
    }
    
    private fun getTopCategory(transactions: List<Transaction>): String {
        return transactions.groupBy { it.category }
            .maxByOrNull { (_, list) -> list.sumOf { it.amount } }
            ?.key ?: "N/A"
    }
    
    companion object {
        fun getDefaultTransaction(): Transaction {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return Transaction(
                amount = 0.0,
                type = TransactionType.EXPENSE,
                category = Categories.FOOD.id,
                accountId = "cash",
                date = now.date,
                merchant = "",
                notes = ""
            )
        }
    }
}