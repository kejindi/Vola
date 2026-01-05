package com.vola.app.di

import android.content.Context
import com.vola.app.data.local.database.VolaDatabase
import com.vola.app.data.local.preferences.UserPreferences
import com.vola.app.data.repository.BudgetRepository
import com.vola.app.data.repository.GoalRepository
import com.vola.app.data.repository.TransactionRepository
import com.vola.app.domain.usecases.CalculateBudgetUseCase
import com.vola.app.domain.usecases.CalculateGoalProgressUseCase
import com.vola.app.domain.usecases.FormatCurrencyUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VolaDatabase {
        return VolaDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideTransactionDao(database: VolaDatabase) = database.transactionDao()
    
    @Provides
    @Singleton
    fun provideBudgetDao(database: VolaDatabase) = database.budgetDao()
    
    @Provides
    @Singleton
    fun provideGoalDao(database: VolaDatabase) = database.goalDao()
    
    @Provides
    @Singleton
    fun provideTransactionRepository(dao: com.vola.app.data.local.database.dao.TransactionDao): TransactionRepository {
        return TransactionRepository(dao)
    }
    
    @Provides
    @Singleton
    fun provideBudgetRepository(dao: com.vola.app.data.local.database.dao.BudgetDao): BudgetRepository {
        return BudgetRepository(dao)
    }
    
    @Provides
    @Singleton
    fun provideGoalRepository(dao: com.vola.app.data.local.database.dao.GoalDao): GoalRepository {
        return GoalRepository(dao)
    }
    
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideCalculateBudgetUseCase(): CalculateBudgetUseCase {
        return CalculateBudgetUseCase()
    }
    
    @Provides
    @Singleton
    fun provideCalculateGoalProgressUseCase(): CalculateGoalProgressUseCase {
        return CalculateGoalProgressUseCase()
    }
    
    @Provides
    @Singleton
    fun provideFormatCurrencyUseCase(): FormatCurrencyUseCase {
        return FormatCurrencyUseCase()
    }
}