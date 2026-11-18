package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "govt_holidays")
data class HolidayEntity(
    @PrimaryKey
    val id: String,
    val countryCode: String,
    val countryName: String,
    val dateIso: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val name: String,
    val localName: String,
    val typesCsv: String,
    val isGlobal: Boolean,
    val reminderEnabled: Boolean = true
)
