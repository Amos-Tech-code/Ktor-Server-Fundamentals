package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

fun Application.configureSessions() {

    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/" //Effective for all routes
            cookie.maxAgeInSeconds = 300
        }
    }
}


@Serializable
data class UserSession(val userName : String)