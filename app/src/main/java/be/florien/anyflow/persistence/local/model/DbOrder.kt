package be.florien.anyflow.persistence.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DbOrder(
        @PrimaryKey
        val priority: Int,
        val subject: Long,
        val orderingType: Int,
        val orderingArgument: Int)