package com.example.incomingcallfcm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM callhistory ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CallHistory>>

    @Insert
    suspend fun insert(callHistory: CallHistory)
}
