package com.absolutefresh.sales.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbox_mutations",
    indices = [
        Index(value = ["operationUuid"], unique = true),
        Index(value = ["status", "createdAt"])
    ]
)
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationUuid: String,
    val entityType: String,
    val entityId: String,
    val action: String,
    val payloadJson: String,
    val status: String = "QUEUED",
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null
)
