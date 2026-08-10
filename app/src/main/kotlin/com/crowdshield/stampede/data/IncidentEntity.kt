package com.crowdshield.stampede.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
