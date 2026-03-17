package com.example.myapp.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapp.DB.AudioRecord

@Dao
interface AudioDao {
    @Insert
    suspend fun insertAudio(audio: AudioRecord)

    @Query("SELECT * FROM audio WHERE userId = :userId ORDER BY id DESC")
    suspend fun getAudiosByUserId(userId: Int): List<AudioRecord>

    @Query("DELETE FROM audio WHERE id = :audioId")
    suspend fun deleteAudioById(audioId: Int)

    @Query("DELETE FROM audio WHERE userId = :userId")
    suspend fun deleteAudiosByUserId(userId: Int)

    @Query("SELECT id FROM audio WHERE audioPath = :audioPath LIMIT 1")
    suspend fun getAudioIdByPath(audioPath: String): Int?
}