package com.example.myapp.Action

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioPath: String? = null
    private val playState = PlayState()
    private var onCompletionListener: (() -> Unit)? = null
    private class PlayState {
        var isPlaying: Boolean = false
    }

    val isPlaying: Boolean
        get() = playState.isPlaying

    fun playAudio(context: Context, audioPath: String): Boolean {
        stopAudio()
        return try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    playState.isPlaying = true
                    currentAudioPath = audioPath
                }
                setOnCompletionListener {
                    onCompletionListener?.invoke()
                    stopAudio()
                }
                setOnErrorListener { _, what, extra ->
                    stopAudio()
                    return@setOnErrorListener true
                }
            }
            true
        } catch (e: IOException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun pauseAudio() {
        if (playState.isPlaying && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            playState.isPlaying = false
        }
    }

    fun resumeAudio() {
        if (!playState.isPlaying && mediaPlayer != null) {
            mediaPlayer?.start()
            playState.isPlaying = true
        }
    }

    fun replayAudio(context: Context): Boolean {
        return currentAudioPath?.let { path ->
            playAudio(context, path)
        } ?: false
    }
    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        playState.isPlaying = false
        currentAudioPath = null
        onCompletionListener = null
    }
    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}