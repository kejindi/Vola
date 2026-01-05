package com.vola.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vola.app.data.models.Budget
import com.vola.app.data.models.Goal
import com.vola.app.data.models.Transaction
import com.vola.app.data.local.database.dao.BudgetDao
import com.vola.app.data.local.database.dao.GoalDao
import com.vola.app.data.local.database.dao.TransactionDao
import com.vola.app.data.local.database.converters.LocalDateConverter
import com.vola.app.data.local.database.converters.LocalTimeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context

@Database(
    entities = [Transaction::class, Budget::class, Goal::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(LocalDateConverter::class, LocalTimeConverter::class)
abstract class VolaDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    
    companion object {
        @Volatile
        private var INSTANCE: VolaDatabase? = null
        
        fun getDatabase(context: Context): VolaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VolaDatabase::class.java,
                    "vola_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                // Pre-populate with default data if needed
                val database = getDatabase(context)
                // Insert default categories or initial data here
            }
        }
    }
}