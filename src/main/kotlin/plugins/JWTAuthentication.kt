package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.*

fun Application.configureJWTAuthentication(config: JWTConfig, httpClient: HttpClient) {
    install(Authentication) {
        jwt("jwt-auth") {
            realm = config.realm

            val jwtVerifier = JWT
                .require(Algorithm.HMAC256(config.secret))
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .build()

            verifier(jwtVerifier)

            validate { jwtCredential ->
                val userName = jwtCredential.payload.getClaim("username").asString()
                if (!userName.isNullOrBlank()) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respondText("Invalid token or expired token", status = HttpStatusCode.Unauthorized)
            }
        }

        configureGoogleOAuth(httpClient)
    }
}


fun generateToken(config: JWTConfig, userName: String) : String {

    return JWT.create()
        .withAudience(config.issuer)
        .withIssuer(config.issuer)
        .withClaim("username", userName)
        .withExpiresAt(Date(System.currentTimeMillis() + config.tokenExpiry))
        .sign(Algorithm.HMAC256(config.secret))
}


data class JWTConfig(
    val realm: String,
    val secret: String,
    val issuer: String,
    val audience: String,
    val tokenExpiry: Long
)