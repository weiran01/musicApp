package com.example.myapp.Action

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioManage(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null

    // 开始录音
    fun startRecording(): Boolean {
        val fileName = "recording_${System.currentTimeMillis()}.m4a"
        val outputFile = File(context.filesDir, fileName)
        currentFilePath = outputFile.absolutePath

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile)
            try {
                prepare()
                start()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
    }

    // 停止录音
    fun stopRecording(): String? {
        return try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            currentFilePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 是否正在录音
    fun isRecording(): Boolean = mediaRecorder != null
}