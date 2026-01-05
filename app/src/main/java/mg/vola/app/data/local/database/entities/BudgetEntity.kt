package mg.vola.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey 
    val id: String,
    val name: String,
    val type: String, // "CASH", "MVOLA", "ORANGE", "AIRTEL"
    val balance: Long, // Stored as Long (MGA) to avoid decimal errors
    val color: Int,
    val icon: String
)
