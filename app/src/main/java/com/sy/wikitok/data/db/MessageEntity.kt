package com.sy.wikitok.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/**
 * @author Yeung
 * @date 2025/7/28
 */
@Entity(
    tableName = "tb_messages",
    foreignKeys = [
        ForeignKey(
            entity = WikiEntity::class,
            parentColumns = ["id"],
            childColumns = ["wikiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("wikiId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wikiId: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)

data class MessageId(val id: Long)

enum class MessageType {
    USER,
    BOT,
    ERROR,
    LOADING
}

class MessageTypeConverter {
    @TypeConverter
    fun fromMessageType(type: MessageType): String {
        return type.name
    }

    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return MessageType.valueOf(value)
    }
}