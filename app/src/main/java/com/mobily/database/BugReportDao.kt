package com.mobily.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class BugReportDao {
    @Insert
    abstract fun insert(item: BugReport):Long

    @Query("SELECT * FROM bugreport_table")
    abstract fun getAllData(): List<BugReport>

    @Query("DELETE FROM bugreport_table")
    abstract fun deleteAll()
}