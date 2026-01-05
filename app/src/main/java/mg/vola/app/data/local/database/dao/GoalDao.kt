package com.vola.app.data.local.database.dao

import androidx.room.*
import com.vola.app.data.models.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface GoalDao {
    
    @Query("SELECT * FROM goals ORDER BY created_at DESC")
    fun getAllGoals(): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: String): Goal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)
    
    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Delete
    suspend fun deleteGoal(goal: Goal)
    
    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)
    
    @Query("SELECT * FROM goals WHERE is_active = :isActive ORDER BY deadline ASC")
    fun getActiveGoals(isActive: Boolean): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE is_pinned = :isPinned ORDER BY created_at DESC")
    fun getPinnedGoals(isPinned: Boolean): Flow<List<Goal>>
    
    @Query("""
        SELECT * FROM goals 
        WHERE deadline BETWEEN :startDate AND :endDate 
        AND is_active = 1
        ORDER BY deadline ASC
    """)
    fun getGoalsByDeadlineRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Goal>>
    
    @Query("""
        SELECT 
            SUM(target_amount) as total_target,
            SUM(current_amount) as total_saved,
            COUNT(*) as goal_count
        FROM goals 
        WHERE is_active = 1
    """)
    suspend fun getGoalsSummary(): GoalsSummary?
    
    @Query("""
        SELECT * FROM goals 
        WHERE current_amount >= target_amount 
        AND is_active = 1
        ORDER BY updated_at DESC
    """)
    fun getCompletedGoals(): Flow<List<Goal>>
    
    @Query("""
        SELECT * FROM goals 
        WHERE deadline <= :date 
        AND current_amount < target_amount
        AND is_active = 1
        ORDER BY deadline ASC
    """)
    fun getOverdueGoals(date: LocalDate): Flow<List<Goal>>
    
    @Query("""
        SELECT * FROM goals 
        WHERE (current_amount / target_amount) >= 0.9 
        AND current_amount < target_amount
        AND is_active = 1
        ORDER BY deadline ASC
    """)
    fun getNearlyCompleteGoals(): Flow<List<Goal>>
    
    @Query("""
        UPDATE goals 
        SET current_amount = current_amount + :amount 
        WHERE id = :id
    """)
    suspend fun addToGoal(id: String, amount: Double)
    
    data class GoalsSummary(
        val total_target: Double = 0.0,
        val total_saved: Double = 0.0,
        val goal_count: Int = 0
    )
}