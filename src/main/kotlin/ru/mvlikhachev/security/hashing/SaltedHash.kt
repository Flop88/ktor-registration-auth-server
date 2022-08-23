package ru.mvlikhachev.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)