package com.example.myapp.Action

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioLogic(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    // 权限检查
    fun hasRecordPermission(): Boolean {
        val permission = android.Manifest.permission.RECORD_AUDIO
        val checkSelfPermission = android.content.pm.PackageManager.PERMISSION_GRANTED // 授权为0/-1
        return context.checkSelfPermission(permission) == checkSelfPermission
    }

    fun startRecording(): Boolean {
        if (!hasRecordPermission()) {
            return false
        }

        val fileName = "recording_${System.currentTimeMillis()}.m4a"
        val outputFile = File(context.filesDir, fileName)
        currentFilePath = outputFile.absolutePath

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
            }

            mediaRecorder?.prepare()
            mediaRecorder?.start()
            return true
        } catch (e: IOException) { // 麦克风被占用，存储不足，无法创建文件，文件路径无效
            Log.e("AudioManage", "录音初始化失败：${e.message}")
            releaseResources()
            return false
        } catch (e: SecurityException) { // 权限
            Log.e("AudioManage", "录音权限不足：${e.message}")
            return false
        }
    }

    // 停止录音
    fun stopRecording(): String? {
        return try {
            mediaRecorder?.stop()
            currentFilePath
        } catch (e: IllegalStateException) {
            Log.e("AudioManage", "停止录音失败：${e.message}")
            null
        } finally {
            releaseResources()
        }
    }

    // 取消录音
    fun cancelRecording() {
        if (mediaRecorder != null) {
            stopRecording()
        }
        currentFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        currentFilePath = null
    }

    private fun releaseResources() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaRecorder = null
        }
    }
    fun isRecording(): Boolean {
        return mediaRecorder != null
    }

    fun getCurrentFilePath(): String? = currentFilePath

    fun onDestroy() {
        if (mediaRecorder != null) {
            releaseResources()
        }
    }
}