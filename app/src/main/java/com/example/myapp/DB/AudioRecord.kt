package com.example.myapp.DB

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "audio",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class AudioRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(index = true) val userId: Int,
    val time: String,
    val title: String,
    val audioPath: String
) {
    constructor(userId: Int, time: String, title: String, audioPath: String) : this(
        id = 0,
        userId = userId,
        time = time,
        title = title,
        audioPath = audioPath
    )
}