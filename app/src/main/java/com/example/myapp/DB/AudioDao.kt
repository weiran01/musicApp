package com.example.myapp.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {
    @Insert
    suspend fun insertAudio(audio: Audio)

    @Query("SELECT id FROM user WHERE username = :username LIMIT 1")
    suspend fun getUserId(username: String): Int?

    // 查用户所有记录
    @Query("SELECT * FROM audio WHERE userId = :userId ORDER BY audioId DESC")
    fun getAudio(userId: Int): Flow<List<Audio>>

    @Query("DELETE FROM audio WHERE audioId = :audioId")
    suspend fun deleteAudio(audioId: Int): Int

    @Query("DELETE FROM audio WHERE userId = :userId")
    suspend fun deleteAll(userId: Int): Int
}