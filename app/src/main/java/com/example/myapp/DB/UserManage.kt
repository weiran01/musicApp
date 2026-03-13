package com.example.myapp.DB

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserManage{
    private fun getUserDao(context: Context): UserDao{
        return AppDatabase.getInstance(context).userDao()
    }

    suspend fun newUser(context: Context,username:String,password:String):Boolean{
        return withContext(Dispatchers.IO) {
            val name = username.trim()
            val pwd = password.trim()

            if (name.isEmpty() || pwd.isEmpty()) return@withContext false

            val dao = getUserDao(context)
            if (dao.findUsername(name) > 0) return@withContext false
            dao.insertUser(User(name, pwd))
            true
        }
    }

    suspend fun loginUser(context: Context, username:String,password:String):Boolean{
        return withContext(Dispatchers.IO) {
            val name = username.trim()
            val pwd = password.trim()

            if (name.isEmpty() || pwd.isEmpty()) return@withContext false

            val dao = getUserDao(context)
            val user = dao.getUser(name)
            user?.password == pwd
        }
    }

    suspend fun updatePassword(context: Context, oldUsername: String, newPassword: String): Boolean {
        return withContext(Dispatchers.IO) {
            val dao = getUserDao(context)
            dao.update_password(oldUsername, newPassword)
        }
    }

    suspend fun updateUsername(context: Context, oldUsername: String, newUsername: String): Boolean {
        return withContext(Dispatchers.IO) {
            val dao = getUserDao(context)
            dao.update_username(oldUsername, newUsername)
        }
    }

    suspend fun updateUser(
        context: Context,
        oldUsername: String,
        newUsername: String,
        newPassword: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val dao = getUserDao(context)
            dao.update_user(oldUsername, newUsername, newPassword)
        }
    }

    suspend fun deleteUser(context: Context, username: String): Boolean {
        return withContext(Dispatchers.IO) {
            val name = username.trim()

            if (name.isEmpty()) return@withContext false

            val dao = getUserDao(context)
            val rows = dao.deleteUser(name)
            rows > 0
        }
    }
}
