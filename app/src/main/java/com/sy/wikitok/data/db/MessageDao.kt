package com.sy.wikitok.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * @author Yeung
 * @date 2025/7/28
 */
@Dao
interface MessageDao {

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Transaction
    suspend fun insertMessageIfNotExists(message: MessageEntity) {
        val existingMessage = readMessages(message.wikiId)
        if (existingMessage.isEmpty()) {
            insertMessage(message)
        }
    }

    @Delete(entity = MessageEntity::class)
    suspend fun removeMessage(msgId: MessageId)

    @Query("SELECT * FROM tb_messages WHERE wikiId = :wikiId ORDER BY timestamp ASC")
    fun observeMessages(wikiId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM tb_messages WHERE wikiId = :wikiId ORDER BY timestamp ASC")
    suspend fun readMessages(wikiId: String): List<MessageEntity>
}