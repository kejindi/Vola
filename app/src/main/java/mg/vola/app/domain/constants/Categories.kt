package com.vola.app.domain.constants

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType
)

object Categories {
    // Expense Categories
    val FOOD = Category("food", "Food", "🍽️", "#FF6B6B", TransactionType.EXPENSE)
    val TRANSPORT = Category("transport", "Transport", "🚌", "#4ECDC4", TransactionType.EXPENSE)
    val UTILITIES = Category("utilities", "Utilities", "💡", "#45B7D1", TransactionType.EXPENSE)
    val SHOPPING = Category("shopping", "Shopping", "🛍️", "#96CEB4", TransactionType.EXPENSE)
    val HEALTH = Category("health", "Health", "🏥", "#FFEAA7", TransactionType.EXPENSE)
    val ENTERTAINMENT = Category("entertainment", "Entertainment", "🎉", "#DDA0DD", TransactionType.EXPENSE)
    val EDUCATION = Category("education", "Education", "📚", "#98D8C8", TransactionType.EXPENSE)
    val HOUSING = Category("housing", "Housing", "🏠", "#6A89CC", TransactionType.EXPENSE)
    val GIFTS = Category("gifts", "Gifts", "💝", "#FF9FF3", TransactionType.EXPENSE)
    val TRAVEL = Category("travel", "Travel", "✈️", "#54A0FF", TransactionType.EXPENSE)
    
    // Income Categories
    val SALARY = Category("salary", "Salary", "💰", "#2E8B57", TransactionType.INCOME)
    val FREELANCE = Category("freelance", "Freelance", "💼", "#2E8B57", TransactionType.INCOME)
    val INVESTMENT = Category("investment", "Investment", "📈", "#2E8B57", TransactionType.INCOME)
    val GIFT_INCOME = Category("gift_income", "Gift", "🎁", "#2E8B57", TransactionType.INCOME)
    val OTHER_INCOME = Category("other_income", "Other", "💸", "#2E8B57", TransactionType.INCOME)
    
    // Transfer Categories
    val SAVINGS = Category("savings", "Savings", "🏦", "#FF9F43", TransactionType.TRANSFER)
    val INVESTMENT_TRANSFER = Category("investment_transfer", "Investment", "📊", "#FF9F43", TransactionType.TRANSFER)
    
    val ALL_CATEGORIES = listOf(
        FOOD, TRANSPORT, UTILITIES, SHOPPING, HEALTH,
        ENTERTAINMENT, EDUCATION, HOUSING, GIFTS, TRAVEL,
        SALARY, FREELANCE, INVESTMENT, GIFT_INCOME, OTHER_INCOME,
        SAVINGS, INVESTMENT_TRANSFER
    )
    
    fun getById(id: String): Category? = ALL_CATEGORIES.find { it.id == id }
    
    fun getExpenseCategories() = ALL_CATEGORIES.filter { it.type == TransactionType.EXPENSE }
    fun getIncomeCategories() = ALL_CATEGORIES.filter { it.type == TransactionType.INCOME }
    fun getTransferCategories() = ALL_CATEGORIES.filter { it.type == TransactionType.TRANSFER }
}