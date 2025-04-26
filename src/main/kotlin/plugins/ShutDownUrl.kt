package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.engine.*

fun Application.configureShutDownUrl() {

    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/shutdown"

        exitCodeSupplier = { 0 }
    }
}