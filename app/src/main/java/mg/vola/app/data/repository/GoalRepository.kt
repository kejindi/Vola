package com.vola.app.data.repository

import com.vola.app.data.local.database.dao.GoalDao
import com.vola.app.data.models.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    
    fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals()
    }
    
    suspend fun getGoalById(id: String): Goal? {
        return goalDao.getGoalById(id)
    }
    
    suspend fun insertGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }
    
    suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }
    
    suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }
    
    suspend fun deleteGoalById(id: String) {
        goalDao.deleteGoalById(id)
    }
    
    fun getActiveGoals(): Flow<List<Goal>> {
        return goalDao.getActiveGoals(true)
    }
    
    fun getCompletedGoals(): Flow<List<Goal>> {
        return goalDao.getActiveGoals(false).map { goals ->
            goals.filter { goal ->
                goal.currentAmount >= goal.targetAmount
            }
        }
    }
    
    fun getPinnedGoals(): Flow<List<Goal>> {
        return goalDao.getPinnedGoals(true)
    }
    
    fun getGoalsByDeadlineRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Goal>> {
        return goalDao.getGoalsByDeadlineRange(startDate, endDate)
    }
    
    suspend fun getGoalsDueSoon(days: Int = 30): List<Goal> {
        val now = LocalDate.now()
        val deadline = now.plusDays(days.toLong())
        
        return goalDao.getGoalsByDeadlineRange(now, deadline).firstOrNull() ?: emptyList()
    }
    
    suspend fun getGoalSummary(): Map<String, Any> {
        val goals = getAllGoals().firstOrNull() ?: emptyList()
        
        val totalGoals = goals.size
        val activeGoals = goals.count { it.currentAmount < it.targetAmount }
        val completedGoals = goals.count { it.currentAmount >= it.targetAmount }
        val totalTarget = goals.sumOf { it.targetAmount }
        val totalSaved = goals.sumOf { it.currentAmount }
        val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget) * 100 else 0.0
        
        return mapOf(
            "totalGoals" to totalGoals,
            "activeGoals" to activeGoals,
            "completedGoals" to completedGoals,
            "totalTarget" to totalTarget,
            "totalSaved" to totalSaved,
            "overallProgress" to overallProgress
        )
    }
    
    suspend fun addToGoal(goalId: String, amount: Double) {
        val goal = getGoalById(goalId) ?: return
        val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
        updateGoal(updatedGoal)
    }
    
    suspend fun transferFromGoal(goalId: String, amount: Double) {
        val goal = getGoalById(goalId) ?: return
        val newAmount = max(0.0, goal.currentAmount - amount)
        val updatedGoal = goal.copy(currentAmount = newAmount)
        updateGoal(updatedGoal)
    }
    
    suspend fun updateGoalProgress(goalId: String, progressPercentage: Double) {
        val goal = getGoalById(goalId) ?: return
        val newAmount = (goal.targetAmount * progressPercentage) / 100
        val updatedGoal = goal.copy(currentAmount = newAmount)
        updateGoal(updatedGoal)
    }
    
    suspend fun toggleGoalPin(goalId: String) {
        val goal = getGoalById(goalId) ?: return
        val updatedGoal = goal.copy(isPinned = !goal.isPinned)
        updateGoal(updatedGoal)
    }
    
    suspend fun archiveGoal(goalId: String) {
        val goal = getGoalById(goalId) ?: return
        val updatedGoal = goal.copy(isActive = false)
        updateGoal(updatedGoal)
    }
    
    suspend fun unarchiveGoal(goalId: String) {
        val goal = getGoalById(goalId) ?: return
        val updatedGoal = goal.copy(isActive = true)
        updateGoal(updatedGoal)
    }
    
    suspend fun duplicateGoal(goal: Goal): Goal {
        val newGoal = goal.copy(
            id = "",
            name = "${goal.name} (Copy)",
            currentAmount = 0.0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        insertGoal(newGoal)
        return newGoal
    }
}