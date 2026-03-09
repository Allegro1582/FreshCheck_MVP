package com.freshcheck.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a food product to be tracked for freshness.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val expiryDate: Long, // Stored as timestamp
    val addedDate: Long = System.currentTimeMillis()
) {
    /**
     * Logic for freshness:
     * - If expired (current time > expiryDate): Red status
     * - If expires in < 3 days: Yellow status
     * - Otherwise: Normal/Green status
     */
    fun getDaysLeft(): Long {
        val diff = expiryDate - System.currentTimeMillis()
        return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    }

    fun isExpired(): Boolean = System.currentTimeMillis() > expiryDate
}
