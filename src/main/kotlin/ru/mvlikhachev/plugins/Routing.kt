package ru.mvlikhachev.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import ru.mvlikhachev.data.UserDataSource
import ru.mvlikhachev.routes.getSecretInfo
import ru.mvlikhachev.routes.signIn
import ru.mvlikhachev.routes.signUp
import ru.mvlikhachev.security.hashing.HashingService
import ru.mvlikhachev.security.token.JwtTokenService
import ru.mvlikhachev.security.token.TokenConfig

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig
) {

    routing {
        signUp(hashingService = hashingService, userDataSource = userDataSource)
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        getSecretInfo()
    }
}
