package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

fun Application.configureSessionAuthentication() {

    install(Authentication) {
        session<UserSession>("session-auth") {
            validate { session ->
                session
            }

            challenge {
                call.respondText(
                    "Unauthorized. Please Login",
                    status = HttpStatusCode.Unauthorized
                )
            }
        }

    }
}