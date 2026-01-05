package mg.vola.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val language: String = "mg",
    val createdAt: Long
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val balance: Long,
    val color: Int
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Long,
    val type: String,
    val category: String,
    val accountId: String,
    val date: Long,
    val merchant: String? = null
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long = 0L,
    val deadline: Long
)
