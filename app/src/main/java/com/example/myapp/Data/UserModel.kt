package com.example.myapp.Data

import java.io.Serializable

data class UserModel(
    val username: String,
    val userpwd: String,
    val userId: Int
): Serializable