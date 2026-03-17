package com.example.myapp.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, AudioRecord::class],
    version = 7,
    exportSchema = false
)
abstract class AudioManage : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun audioDao(): AudioDao

    companion object {
        @Volatile
        private var INSTANCE: AudioManage? = null

        fun getInstance(context: Context): AudioManage {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioManage::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
