package ru.mvlikhachev.security.token

data class TokenClaim(
    val name: String,
    val value: String
)