package ru.mvlikhachev.data

import ru.mvlikhachev.data.models.User

interface UserDataSource {

    suspend fun getUserByUsername(username: String): User?

    suspend fun insertUser(user: User): Boolean
}