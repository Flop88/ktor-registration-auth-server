package ru.mvlikhachev.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.mvlikhachev.data.UserDataSource
import ru.mvlikhachev.data.models.User
import ru.mvlikhachev.data.models.requests.AuthRequest
import ru.mvlikhachev.data.models.responses.AuthResponse
import ru.mvlikhachev.security.hashing.HashingService
import ru.mvlikhachev.security.hashing.SaltedHash
import ru.mvlikhachev.security.token.JwtTokenService
import ru.mvlikhachev.security.token.TokenClaim
import ru.mvlikhachev.security.token.TokenConfig

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPasswordShort = request.password.length < 8

        if (userDataSource.getUserByUsername(request.username) != null) {
            call.respond(HttpStatusCode.Conflict, "Username is already exists")
            return@post
        }

        if (areFieldsBlank || isPasswordShort) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        } else {
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val user = userDataSource.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}


fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your id: $userId")
        }
    }
}