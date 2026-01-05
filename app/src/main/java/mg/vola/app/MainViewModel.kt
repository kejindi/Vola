package com.vola.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vola.app.data.models.*
import com.vola.app.data.repository.TransactionRepository
import com.vola.app.data.repository.BudgetRepository
import com.vola.app.data.repository.GoalRepository
import com.vola.app.data.local.preferences.UserPreferences
import com.vola.app.domain.usecases.CalculateBudgetUseCase
import com.vola.app.domain.usecases.CalculateGoalProgressUseCase
import com.vola.app.domain.usecases.FormatCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    private val userPreferences: UserPreferences,
    private val calculateBudgetUseCase: CalculateBudgetUseCase,
    private val calculateGoalProgressUseCase: CalculateGoalProgressUseCase,
    private val formatCurrencyUseCase: FormatCurrencyUseCase
) : ViewModel() {
    
    // UI State
    data class UiState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        val transactions: List<Transaction> = emptyList(),
        val budgets: List<Budget> = emptyList(),
        val goals: List<Goal> = emptyList(),
        val totalIncome: Double = 0.0,
        val totalExpense: Double = 0.0,
        val totalSavings: Double = 0.0,
        val budgetSummary: BudgetSummary = BudgetSummary(),
        val goalSummary: GoalSummary = GoalSummary(),
        val recentTransactions: List<Transaction> = emptyList(),
        val currency: String = "MGA",
        val userName: String = "",
        val userEmail: String = "",
        val monthlyIncome: Double = 0.0,
        val savingsTarget: Double = 0.0,
        val isOnboardingComplete: Boolean = false,
        val isDarkMode: Boolean = false,
        val isBiometricEnabled: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        observePreferences()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load all data in parallel
                val transactions = transactionRepository.getAllTransactions().first()
                val budgets = budgetRepository.getAllBudgets().first()
                val goals = goalRepository.getAllGoals().first()
                
                // Calculate summaries
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val startDate = LocalDate(now.year, now.monthNumber, 1)
                val endDate = if (now.monthNumber == 12) {
                    LocalDate(now.year + 1, 1, 1).minusDays(1)
                } else {
                    LocalDate(now.year, now.monthNumber + 1, 1).minusDays(1)
                }
                
                val transactionSummary = transactionRepository.getTransactionSummary(startDate, endDate)
                val budgetSummary = calculateBudgetUseCase(budgets, transactions, startDate, endDate)
                val goalSummary = calculateGoalProgressUseCase(goals)
                val recentTransactions = transactionRepository.getRecentTransactions(5)
                
                // Load user preferences
                val userName = userPreferences.getUserName() ?: ""
                val userEmail = userPreferences.dataStore.data
                    .map { it[androidx.datastore.preferences.core.stringPreferencesKey("user_email")] ?: "" }
                    .first()
                
                val monthlyIncome = userPreferences.monthlyIncome.first()
                val savingsTarget = userPreferences.dataStore.data
                    .map { it[androidx.datastore.preferences.core.doublePreferencesKey("savings_target")] ?: 0.0 }
                    .first()
                
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        transactions = transactions,
                        budgets = budgets,
                        goals = goals,
                        totalIncome = transactionSummary.totalIncome,
                        totalExpense = transactionSummary.totalExpense,
                        totalSavings = transactionSummary.netAmount,
                        budgetSummary = budgetSummary,
                        goalSummary = goalSummary,
                        recentTransactions = recentTransactions,
                        userName = userName,
                        userEmail = userEmail,
                        monthlyIncome = monthlyIncome,
                        savingsTarget = savingsTarget
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load data"
                    )
                }
            }
        }
    }
    
    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferences.selectedCurrency,
                userPreferences.isOnboardingComplete,
                userPreferences.isDarkMode,
                userPreferences.isBiometricEnabled
            ) { currency, onboardingComplete, darkMode, biometricEnabled ->
                _uiState.update { state ->
                    state.copy(
                        currency = currency,
                        isOnboardingComplete = onboardingComplete,
                        isDarkMode = darkMode,
                        isBiometricEnabled = biometricEnabled
                    )
                }
            }.collect()
        }
    }
    
    fun formatCurrency(amount: Double): String {
        return formatCurrencyUseCase(amount, _uiState.value.currency)
    }
    
    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    suspend fun updateMonthlyIncome(income: Double) {
        userPreferences.setMonthlyIncome(income)
        _uiState.update { it.copy(monthlyIncome = income) }
    }
    
    suspend fun updateSavingsTarget(target: Double) {
        userPreferences.setSavingsTarget(target)
        _uiState.update { it.copy(savingsTarget = target) }
    }
    
    suspend fun updateUserInfo(name: String, email: String) {
        userPreferences.setUserInfo(name, email)
        _uiState.update { it.copy(userName = name, userEmail = email) }
    }
    
    suspend fun completeOnboarding() {
        userPreferences.setOnboardingComplete(true)
        _uiState.update { it.copy(isOnboardingComplete = true) }
    }
    
    fun getCurrentMonth(): Pair<LocalDate, LocalDate> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val startDate = LocalDate(now.year, now.monthNumber, 1)
        val endDate = if (now.monthNumber == 12) {
            LocalDate(now.year + 1, 1, 1).minusDays(1)
        } else {
            LocalDate(now.year, now.monthNumber + 1, 1).minusDays(1)
        }
        return Pair(startDate, endDate)
    }
    
    fun getCategorySpending(category: String): Double {
        val (startDate, endDate) = getCurrentMonth()
        return _uiState.value.transactions
            .filter { 
                it.date >= startDate && 
                it.date <= endDate && 
                it.category == category && 
                it.type == TransactionType.EXPENSE 
            }
            .sumOf { it.amount }
    }
    
    fun getTopSpendingCategories(limit: Int = 5): List<Pair<String, Double>> {
        val (startDate, endDate) = getCurrentMonth()
        return _uiState.value.transactions
            .filter { it.date >= startDate && it.date <= endDate && it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { (_, amount) -> amount }
            .take(limit)
    }
    
    fun getSpendingTrend(): List<Double> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val last6Months = (0..5).map { i ->
            val month = now.monthNumber - i
            val year = if (month <= 0) now.year - 1 else now.year
            val adjustedMonth = if (month <= 0) month + 12 else month
            Pair(year, adjustedMonth)
        }.reversed()
        
        return last6Months.map { (year, month) ->
            val startDate = LocalDate(year, month, 1)
            val endDate = if (month == 12) {
                LocalDate(year + 1, 1, 1).minusDays(1)
            } else {
                LocalDate(year, month + 1, 1).minusDays(1)
            }
            
            _uiState.value.transactions
                .filter { 
                    it.date >= startDate && 
                    it.date <= endDate && 
                    it.type == TransactionType.EXPENSE 
                }
                .sumOf { it.amount }
        }
    }
    
    fun getSavingsRate(): Double {
        val totalIncome = _uiState.value.totalIncome
        val totalExpense = _uiState.value.totalExpense
        return if (totalIncome > 0) {
            ((totalIncome - totalExpense) / totalIncome) * 100
        } else {
            0.0
        }
    }
}