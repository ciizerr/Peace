package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE reminderId = :reminderId ORDER BY timestamp ASC")
    fun getAttachmentsForReminder(reminderId: Int): Flow<List<AttachmentEntity>>
    
    @Query("SELECT * FROM attachments ORDER BY timestamp DESC")
    fun getAllAttachments(): Flow<List<AttachmentEntity>>
    
    @Insert
    suspend fun insert(attachment: AttachmentEntity): Long
    
    @Delete
    suspend fun delete(attachment: AttachmentEntity)
}
