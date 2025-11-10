package com.example.incomingcallfcm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM message ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Message>>

    @Query("""
        SELECT name,
               MAX(timestamp) as lastMessageTime,
               COUNT(CASE WHEN isRead = 0 THEN 1 END) as unreadCount,
               subject as lastSubject
        FROM message
        GROUP BY name
        ORDER BY lastMessageTime DESC
    """)
    fun getConversationSummaries(): Flow<List<ConversationSummary>>

    @Query("SELECT * FROM message WHERE name = :name ORDER BY timestamp ASC")
    fun getMessagesByName(name: String): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE name = :name AND isRead = 0 ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstUnreadMessage(name: String): Message?

    @Update
    suspend fun update(message: Message)

    @Query("UPDATE message SET isRead = 1 WHERE name = :name")
    suspend fun markAllAsRead(name: String)
}

data class ConversationSummary(
    val name: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val lastSubject: String
)
