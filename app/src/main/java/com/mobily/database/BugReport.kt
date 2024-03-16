package com.mobily.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bugreport_table")
data class BugReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val imageUrl: String
)