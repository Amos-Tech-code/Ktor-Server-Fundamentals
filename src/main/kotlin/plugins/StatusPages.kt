package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {

    install(StatusPages) {

        exception<Throwable> { call, cause->
            call.respondText("500: ${cause.message}", status = HttpStatusCode.InternalServerError)
        }

        status(HttpStatusCode.Unauthorized) { call, cause->
            call.respondText("401: You are not unauthorized to access this resource", status = HttpStatusCode.Unauthorized)
        }

        status(HttpStatusCode.BadRequest) { call, cause->
            call.respondText("400: ${cause.description}", status = HttpStatusCode.BadRequest)
        }

        statusFile(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized, HttpStatusCode.NotFound, filePattern = "errors/error#.html")

    }
}