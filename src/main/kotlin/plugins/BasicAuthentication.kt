package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.*

fun Application.configureBasicAuthentication() {

    val hashedUserTableAuth = createHashedUserTable()

    install(Authentication) {
        basic("basic-auth") {
            validate { credentials ->
//                val username = credentials.name
//                val password = credentials.password
//
//                if (username == "admin" && password == "password") {
//                    UserIdPrincipal(username)
//                } else {
//                    null
//                }

                hashedUserTableAuth.authenticate(credentials)
            }
        }
    }
}

fun createHashedUserTable () : UserHashedTableAuth{

    val digestFunction = getDigestFunction("SHA-256"){ "ktor${it.length}" }

    return UserHashedTableAuth(
        digester = digestFunction,
        table = mapOf(
            "admin" to digestFunction("password"),
            "user" to digestFunction("123")
        )
    )
}