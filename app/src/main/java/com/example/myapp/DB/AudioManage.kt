package com.example.myapp.DB

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

object AudioManage {

    private fun getAudioDao(context: Context): AudioDao {
        return AppDatabase.getInstance(context).audioDao()
    }

    suspend fun addAudio(
        context: Context,
        username: String,
        time: String,
        title: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val userId = getAudioDao(context).getUserId(username)
                ?: return@withContext false

            val audio = Audio(userId, time, title)
            getAudioDao(context).insertAudio(audio)
            true
        }
    }

    fun getAudioList(context: Context, username: String): Flow<List<Audio>> {
        return flow {
            val userId = withContext(Dispatchers.IO) {
                getAudioDao(context).getUserId(username) ?: 0
            }
            emitAll(getAudioDao(context).getAudio(userId))
        }
    }

    suspend fun deleteAudio(context: Context, audioId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val rows = getAudioDao(context).deleteAudio(audioId)
            rows > 0
        }
    }

    //注销
    suspend fun deleteAllUserAudios(context: Context, username: String): Boolean {
        return withContext(Dispatchers.IO) {
            val userId = getAudioDao(context).getUserId(username)
                ?: return@withContext false
            val rows = getAudioDao(context).deleteAll(userId)
            rows > 0
        }
    }
}