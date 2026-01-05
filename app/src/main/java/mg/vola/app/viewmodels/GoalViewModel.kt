package com.vola.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vola.app.data.models.Goal
import com.vola.app.data.models.GoalProgress
import com.vola.app.data.models.Milestone
import com.vola.app.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.math.max
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {
    
    data class GoalState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val goals: List<Goal> = emptyList(),
        val goalProgresses: List<GoalProgress> = emptyList(),
        val selectedGoal: Goal? = null,
        val showCreateDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showCelebration: Boolean = false,
        val completedGoal: Goal? = null,
        val filterActive: Boolean = true,
        val filterCompleted: Boolean = false,
        val filterPinned: Boolean = false,
        val totalGoals: Int = 0,
        val activeGoals: Int = 0,
        val completedGoals: Int = 0,
        val totalTarget: Double = 0.0,
        val totalSaved: Double = 0.0,
        val overallProgress: Double = 0.0,
        val goalSuggestions: List<Goal> = emptyList()
    )
    
    private val _state = MutableStateFlow(GoalState())
    val state: StateFlow<GoalState> = _state.asStateFlow()
    
    init {
        loadGoals()
    }
    
    fun loadGoals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val goals = goalRepository.getAllGoals().first()
                calculateGoalProgresses(goals)
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load goals"
                    )
                }
            }
        }
    }
    
    private fun calculateGoalProgresses(goals: List<Goal>) {
        val progresses = goals.map { goal ->
            calculateGoalProgress(goal)
        }
        
        val activeGoals = progresses.filter { !it.progressPercentage.isNaN() && it.progressPercentage < 100 }
        val completedGoals = progresses.filter { it.progressPercentage >= 100 }
        
        val totalTarget = goals.sumOf { it.targetAmount }
        val totalSaved = goals.sumOf { it.currentAmount }
        val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget) * 100 else 0.0
        
        _state.update { state ->
            state.copy(
                isLoading = false,
                goals = goals,
                goalProgresses = progresses,
                totalGoals = goals.size,
                activeGoals = activeGoals.size,
                completedGoals = completedGoals.size,
                totalTarget = totalTarget,
                totalSaved = totalSaved,
                overallProgress = overallProgress
            )
        }
    }
    
    private fun calculateGoalProgress(goal: Goal): GoalProgress {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val progressPercentage = (goal.currentAmount / goal.targetAmount) * 100
        
        // Calculate remaining time
        val monthsRemaining = max(0, ceil((goal.targetAmount - goal.currentAmount) / goal.monthlyContribution).toInt())
        val projectedCompletion = now.plusMonths(monthsRemaining.toLong())
        
        // Check if on track
        val expectedProgress = if (goal.createdAt > 0) {
            val monthsPassed = now.toEpochDays().minus(LocalDate.fromEpochDays(goal.createdAt / 86400000).toEpochDays()) / 30
            goal.monthlyContribution * monthsPassed
        } else {
            goal.currentAmount
        }
        val isOnTrack = goal.currentAmount >= expectedProgress * 0.9 // 10% tolerance
        
        // Calculate milestones (25%, 50%, 75%, 100%)
        val milestones = listOf(0.25, 0.5, 0.75, 1.0).map { percentage ->
            val milestoneAmount = goal.targetAmount * percentage
            val achieved = goal.currentAmount >= milestoneAmount
            Milestone(
                percentage = percentage * 100,
                amount = milestoneAmount,
                achieved = achieved,
                achievedAt = if (achieved) now else null
            )
        }
        
        return GoalProgress(
            goal = goal,
            progressPercentage = progressPercentage,
            monthsRemaining = monthsRemaining,
            monthlyRequired = goal.monthlyContribution,
            isOnTrack = isOnTrack,
            projectedCompletion = projectedCompletion,
            milestones = milestones
        )
    }
    
    fun selectGoal(goal: Goal?) {
        _state.update { it.copy(selectedGoal = goal) }
    }
    
    fun showCreateDialog(show: Boolean) {
        _state.update { it.copy(showCreateDialog = show) }
    }
    
    fun showDeleteDialog(show: Boolean) {
        _state.update { it.copy(showDeleteDialog = show) }
    }
    
    fun showCelebration(show: Boolean, goal: Goal? = null) {
        _state.update { it.copy(showCelebration = show, completedGoal = goal) }
    }
    
    fun setFilterActive(active: Boolean) {
        _state.update { it.copy(filterActive = active) }
        applyFilters()
    }
    
    fun setFilterCompleted(completed: Boolean) {
        _state.update { it.copy(filterCompleted = completed) }
        applyFilters()
    }
    
    fun setFilterPinned(pinned: Boolean) {
        _state.update { it.copy(filterPinned = pinned) }
        applyFilters()
    }
    
    private fun applyFilters() {
        val state = _state.value
        val filteredGoals = state.goals.filter { goal ->
            val progress = calculateGoalProgress(goal)
            var matches = true
            
            if (state.filterActive) {
                matches = matches && progress.progressPercentage < 100
            }
            
            if (state.filterCompleted) {
                matches = matches && progress.progressPercentage >= 100
            }
            
            if (state.filterPinned) {
                matches = matches && goal.isPinned
            }
            
            matches
        }
        
        calculateGoalProgresses(filteredGoals)
    }
    
    suspend fun saveGoal(goal: Goal) {
        try {
            val isNewGoal = goal.id.isBlank()
            if (isNewGoal) {
                goalRepository.insertGoal(goal)
            } else {
                goalRepository.updateGoal(goal)
            }
            
            loadGoals()
            
            // Check if goal was completed
            if (!isNewGoal && goal.currentAmount >= goal.targetAmount) {
                showCelebration(true, goal)
            }
        } catch (e: Exception) {
            throw Exception("Failed to save goal: ${e.message}")
        }
    }
    
    suspend fun deleteGoal(goal: Goal) {
        try {
            goalRepository.deleteGoal(goal)
            loadGoals()
        } catch (e: Exception) {
            throw Exception("Failed to delete goal: ${e.message}")
        }
    }
    
    suspend fun addToGoal(goalId: String, amount: Double) {
        try {
            val goal = goalRepository.getGoalById(goalId) ?: return
            val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
            goalRepository.updateGoal(updatedGoal)
            loadGoals()
        } catch (e: Exception) {
            throw Exception("Failed to add to goal: ${e.message}")
        }
    }
    
    suspend fun togglePinGoal(goalId: String) {
        try {
            val goal = goalRepository.getGoalById(goalId) ?: return
            val updatedGoal = goal.copy(isPinned = !goal.isPinned)
            goalRepository.updateGoal(updatedGoal)
            loadGoals()
        } catch (e: Exception) {
            throw Exception("Failed to toggle pin: ${e.message}")
        }
    }
    
    fun calculateMonthlyContribution(
        targetAmount: Double,
        deadline: LocalDate,
        currentAmount: Double = 0.0
    ): Double {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val monthsRemaining = max(1, now.daysUntil(deadline) / 30)
        val remainingAmount = targetAmount - currentAmount
        return remainingAmount / monthsRemaining
    }
    
    fun calculateTimeline(
        targetAmount: Double,
        monthlyContribution: Double,
        currentAmount: Double = 0.0
    ): Map<String, Any> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val remainingAmount = targetAmount - currentAmount
        val monthsNeeded = ceil(remainingAmount / monthlyContribution).toInt()
        val projectedCompletion = now.plusMonths(monthsNeeded.toLong())
        
        return mapOf(
            "monthsNeeded" to monthsNeeded,
            "projectedCompletion" to projectedCompletion,
            "isAchievable" to monthsNeeded <= 60, // 5 years max
            "monthlyContribution" to monthlyContribution
        )
    }
    
    fun generateGoalSuggestions() {
        val suggestions = listOf(
            Goal(
                name = "Emergency Fund",
                targetAmount = 300000.0,
                deadline = LocalDate(2024, 12, 31),
                monthlyContribution = 50000.0,
                icon = "🏠",
                color = "#FF6B35"
            ),
            Goal(
                name = "Education Fund",
                targetAmount = 500000.0,
                deadline = LocalDate(2025, 6, 30),
                monthlyContribution = 25000.0,
                icon = "🎓",
                color = "#2E8B57"
            ),
            Goal(
                name = "Vacation",
                targetAmount = 600000.0,
                deadline = LocalDate(2024, 8, 31),
                monthlyContribution = 100000.0,
                icon = "🏖️",
                color = "#54A0FF"
            ),
            Goal(
                name = "New Phone",
                targetAmount = 800000.0,
                deadline = LocalDate(2024, 10, 31),
                monthlyContribution = 133333.0,
                icon = "📱",
                color = "#0047AB"
            )
        )
        
        _state.update { it.copy(goalSuggestions = suggestions) }
    }
    
    fun getGoalProgress(goalId: String): GoalProgress? {
        return _state.value.goalProgresses.find { it.goal.id == goalId }
    }
    
    fun getFilteredGoals(): List<Goal> {
        val state = _state.value
        return state.goals.filter { goal ->
            val progress = calculateGoalProgress(goal)
            
            var matches = true
            if (state.filterActive) {
                matches = matches && progress.progressPercentage < 100
            }
            if (state.filterCompleted) {
                matches = matches && progress.progressPercentage >= 100
            }
            if (state.filterPinned) {
                matches = matches && goal.isPinned
            }
            matches
        }
    }
}