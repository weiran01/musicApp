package com.example.myapp.Data

import java.io.Serializable

data class RecordModel(
    val time: String,
    val title: String,
    val audioPath: String? = null,
    val audioId: Int = 0
): Serializable