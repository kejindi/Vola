package mg.vola.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val amount: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val accountId: String,
    val date: Long, // Unix timestamp
    val note: String? = null
)
