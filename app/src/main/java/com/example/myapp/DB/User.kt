package com.example.myapp.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    indices = [androidx.room.Index(value = ["username"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int=1,
    val username:String,
    val password:String
){
    constructor(username:String,password:String):this(
        id=0,
        username=username,
        password=password
    )
}
