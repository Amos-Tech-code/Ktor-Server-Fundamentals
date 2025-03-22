package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimiting() {

    install(RateLimit) {

//        global {
//            rateLimiter(limit = 5, refillPeriod = 60.seconds)
//        }

        register(RateLimitName("public")) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
        }

        register(RateLimitName("protected")) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
            requestKey { call ->
                call.request.queryParameters["type"] ?: ""
            }

            requestWeight { applicationCall, key ->
                when (key) {
                    "admin" -> 2
                    else -> 1
                }
            }
        }
    }
}