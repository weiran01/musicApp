package com.example.myapp.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserDao{
    @Insert
    suspend fun insertUser(users:User)

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    suspend fun getUserId(userId: Int): User?

    @Query("SELECT COUNT(*) FROM user WHERE username = :username")
    suspend fun findUsername(username: String): Int

    @Query("UPDATE user SET password = :newPassword WHERE username = :oldUsername")
    suspend fun updatePwd(oldUsername: String, newPassword: String): Int

    @Query("UPDATE user SET username=:newUsername WHERE username = :oldUsername")
    suspend fun updateUsername(oldUsername: String,newUsername:String):Int

    @Query("DELETE FROM user WHERE username = :username")
    suspend fun deleteUser(username: String): Int

    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): User?

    @Transaction
    suspend fun update_password(oldUsername: String, newPassword: String): Boolean {
        val pwd = newPassword.trim()
        if (pwd.isEmpty()) return false

        val oldUser = getUser(oldUsername) ?: return false
        val updateCount = updatePwd(oldUsername, pwd)

        return updateCount > 0
    }

    @Transaction
    suspend fun update_username(oldUsername: String, newUsername: String): Boolean {

        val oldName = oldUsername.trim()
        val newName = newUsername.trim()
        if (oldName.isEmpty() || newName.isEmpty()) return false

        val oldUser = getUser(oldName) ?: return false
        if (findUsername(newName) > 0) return false

        val updateCount = updateUsername(oldName, newName)
        return updateCount > 0
    }

    @Transaction
    suspend fun update_user(
        oldUsername: String,
        newUsername: String,
        newPassword: String
    ): Boolean {

        val oldName = oldUsername.trim()
        val newName = newUsername.trim()
        val newPwd = newPassword.trim()
        if (oldName.isEmpty() || newName.isEmpty() || newPwd.isEmpty()) return false

        val oldUser = getUser(oldName) ?: return false
        if (findUsername(newName) > 0) return false

        val nameUpdateCount = updateUsername(oldName, newName)
        if (nameUpdateCount == 0) return false

        val pwdUpdateCount = updatePwd(newName, newPwd)
        return pwdUpdateCount > 0
    }
}
