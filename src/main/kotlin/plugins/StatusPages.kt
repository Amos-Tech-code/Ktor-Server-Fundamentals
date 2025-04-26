package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {

    install(StatusPages) {

        //Validation Error
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("errors" to cause.reasons))
        }

        exception<Throwable> { call, cause->
            call.respondText("500: ${cause.message}", status = HttpStatusCode.InternalServerError)
        }

        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"]
            call.respondText(text = "429: Too many request. Wait after $retryAfter seconds.")
        }

//        status(HttpStatusCode.Unauthorized) { call, cause->
//            call.respondText("401: You are not unauthorized to access this resource", status = HttpStatusCode.Unauthorized)
//        }

        status(HttpStatusCode.BadRequest) { call, cause->
            call.respondText("400: ${cause.description}", status = HttpStatusCode.BadRequest)
        }

        statusFile(HttpStatusCode.BadRequest, /*HttpStatusCode.Unauthorized,*/ HttpStatusCode.NotFound, filePattern = "errors/error#.html")

    }
}