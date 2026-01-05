package com.vola.app.domain.usecases

import com.vola.app.data.models.Goal
import com.vola.app.data.models.GoalProgress
import com.vola.app.data.models.GoalSummary
import com.vola.app.data.models.Milestone
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.math.max
import javax.inject.Inject

class CalculateGoalProgressUseCase @Inject constructor() {
    
    operator fun invoke(goals: List<Goal>): GoalSummary {
        val activeGoals = goals.filter { it.currentAmount < it.targetAmount }
        val completedGoals = goals.filter { it.currentAmount >= it.targetAmount }
        
        val totalTarget = goals.sumOf { it.targetAmount }
        val totalSaved = goals.sumOf { it.currentAmount }
        val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget) * 100 else 0.0
        
        return GoalSummary(
            totalGoals = goals.size,
            activeGoals = activeGoals.size,
            completedGoals = completedGoals.size,
            totalTarget = totalTarget,
            totalSaved = totalSaved,
            overallProgress = overallProgress
        )
    }
    
    fun calculateGoalProgress(goal: Goal): GoalProgress {
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
        
        // Calculate milestones
        val milestones = calculateMilestones(goal)
        
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
    
    private fun calculateMilestones(goal: Goal): List<Milestone> {
        val milestones = listOf(0.25, 0.5, 0.75, 1.0)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        return milestones.map { percentage ->
            val milestoneAmount = goal.targetAmount * percentage
            val achieved = goal.currentAmount >= milestoneAmount
            Milestone(
                percentage = percentage * 100,
                amount = milestoneAmount,
                achieved = achieved,
                achievedAt = if (achieved) now else null
            )
        }
    }
    
    fun calculateRequiredMonthlyContribution(
        targetAmount: Double,
        deadline: LocalDate,
        currentAmount: Double = 0.0
    ): Double {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val monthsRemaining = max(1, now.daysUntil(deadline) / 30)
        val remainingAmount = targetAmount - currentAmount
        return remainingAmount / monthsRemaining
    }
    
    fun calculateProjectedCompletion(
        goal: Goal,
        additionalMonthlyContribution: Double = 0.0
    ): LocalDate {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val totalMonthly = goal.monthlyContribution + additionalMonthlyContribution
        val monthsNeeded = ceil((goal.targetAmount - goal.currentAmount) / totalMonthly).toInt()
        return now.plusMonths(monthsNeeded.toLong())
    }
    
    fun isGoalAchievable(
        goal: Goal,
        monthlyIncome: Double,
        currentMonthlyCommitments: Double
    ): Boolean {
        val disposableIncome = monthlyIncome - currentMonthlyCommitments
        return goal.monthlyContribution <= disposableIncome * 0.3 // Max 30% of disposable income
    }
    
    fun calculateGoalPriority(goal: Goal): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val daysRemaining = now.daysUntil(goal.deadline)
        val progressPercentage = (goal.currentAmount / goal.targetAmount) * 100
        
        // Priority calculation based on urgency and progress
        val urgencyScore = when {
            daysRemaining < 30 -> 3
            daysRemaining < 90 -> 2
            else -> 1
        }
        
        val progressScore = when {
            progressPercentage > 75 -> 1
            progressPercentage > 50 -> 2
            else -> 3
        }
        
        return urgencyScore * progressScore
    }
    
    fun suggestGoalAmount(
        goalType: String,
        monthlyIncome: Double
    ): Double {
        return when (goalType.lowercase()) {
            "emergency" -> monthlyIncome * 3 // 3 months of income
            "vacation" -> monthlyIncome * 0.5 // Half month income
            "education" -> monthlyIncome * 6 // 6 months of income
            "vehicle" -> monthlyIncome * 12 // 1 year of income
            "housing" -> monthlyIncome * 24 // 2 years of income
            else -> monthlyIncome * 2 // Default: 2 months of income
        }
    }
}