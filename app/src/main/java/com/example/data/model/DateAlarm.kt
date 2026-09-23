package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "date_alarms")
data class DateAlarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int, // 0 - 11
    val day: Int, // 1 - 31
    val hour: Int, // 0 - 23
    val minute: Int, // 0 - 59
    val title: String,
    val description: String = "",
    val triggerTimeMillis: Long,
    val isEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
