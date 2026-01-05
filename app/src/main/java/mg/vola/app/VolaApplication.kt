package com.vola.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.vola.app.workers.BackupWorker
import com.vola.app.workers.BudgetNotificationWorker
import com.vola.app.workers.GoalReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class VolaApplication : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "vola_notifications"
        const val NOTIFICATION_CHANNEL_NAME = "Vola Notifications"
        const val BACKUP_WORK_NAME = "vola_backup_work"
        const val BUDGET_WORK_NAME = "vola_budget_work"
        const val GOAL_WORK_NAME = "vola_goal_work"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channel for Android O and above
        createNotificationChannel()
        
        // Schedule periodic workers
        scheduleWorkers()
        
        // Initialize any third-party libraries here
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for budget alerts, goal reminders, and transaction updates"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 200, 300)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun scheduleWorkers() {
        // Backup worker - runs daily at 2 AM
        val backupWorkRequest = PeriodicWorkRequest.Builder(
            BackupWorker::class.java,
            24, // Repeat interval
            TimeUnit.HOURS
        )
            .setInitialDelay(2, TimeUnit.HOURS) // Start after 2 hours
            .build()
        
        // Budget notification worker - runs daily at 9 AM
        val budgetWorkRequest = PeriodicWorkRequest.Builder(
            BudgetNotificationWorker::class.java,
            24, // Repeat interval
            TimeUnit.HOURS
        )
            .setInitialDelay(9, TimeUnit.HOURS) // Start after 9 hours
            .build()
        
        // Goal reminder worker - runs weekly on Monday at 8 AM
        val goalWorkRequest = PeriodicWorkRequest.Builder(
            GoalReminderWorker::class.java,
            7, // Repeat interval
            TimeUnit.DAYS
        )
            .setInitialDelay(1, TimeUnit.DAYS) // Start after 1 day
            .build()
        
        WorkManager.getInstance(this).apply {
            enqueueUniquePeriodicWork(
                BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                backupWorkRequest
            )
            
            enqueueUniquePeriodicWork(
                BUDGET_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                budgetWorkRequest
            )
            
            enqueueUniquePeriodicWork(
                GOAL_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                goalWorkRequest
            )
        }
    }
    
    fun clearAllWorkers() {
        WorkManager.getInstance(this).apply {
            cancelUniqueWork(BACKUP_WORK_NAME)
            cancelUniqueWork(BUDGET_WORK_NAME)
            cancelUniqueWork(GOAL_WORK_NAME)
        }
    }
}