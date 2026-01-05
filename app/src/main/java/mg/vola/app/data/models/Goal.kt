package com.vola.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: LocalDate,
    val monthlyContribution: Double,
    val icon: String = "🎯",
    val color: String = "#2E8B57",
    val isPinned: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class GoalProgress(
    val goal: Goal,
    val progressPercentage: Double,
    val monthsRemaining: Int,
    val monthlyRequired: Double,
    val isOnTrack: Boolean,
    val projectedCompletion: LocalDate,
    val milestones: List<Milestone>
)

data class Milestone(
    val percentage: Double,
    val amount: Double,
    val achieved: Boolean,
    val achievedAt: LocalDate? = null
)

data class GoalSummary(
    val totalGoals: Int = 0,
    val activeGoals: Int = 0,
    val completedGoals: Int = 0,
    val totalTarget: Double = 0.0,
    val totalSaved: Double = 0.0,
    val overallProgress: Double = 0.0
)