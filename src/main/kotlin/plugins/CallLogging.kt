package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureCallLogging() {

    install(CallLogging) {
        level = Level.INFO

        //filter { call -> call.request.path().startsWith("/hi") }

        format {call ->
            val userId = call.request.queryParameters["userId"] ?: "Unknown"
            "UserID: $userId, Method: ${call.request.httpMethod.value}, Path: ${call.request.path()}"
        }

    }
}