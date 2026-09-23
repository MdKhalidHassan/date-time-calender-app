package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "date_notes")
data class DateNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int, // 0 - 11
    val day: Int, // 1 - 31
    val hour: Int = 0, // 0 - 23
    val minute: Int = 0, // 0 - 59
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val updatedHour: Int? = null,
    val updatedMinute: Int? = null,
    val updatedYear: Int? = null,
    val updatedMonth: Int? = null,
    val updatedDay: Int? = null,
    val editCount: Int = 0
)

