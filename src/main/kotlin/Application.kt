package com.example

import com.example.plugins.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    val jwt = environment.config.config("ktor.jwt")
    val realm = jwt.property("realm").getString()
    val secret = jwt.property("secret").getString()
    val issuer = jwt.property("issuer").getString()
    val audience = jwt.property("audience").getString()
    val tokenExpiry = jwt.property("expiry").getString().toLong()

    val config = JWTConfig(
        realm = realm,
        issuer = issuer,
        audience = audience,
        tokenExpiry = tokenExpiry,
        secret = secret
    )

    configureCallLogging()

    configureResources()
    configureRateLimiting()
    //configureBasicAuthentication()
    //configureDigestAuthentication()
    //configureBearerAuthentication()
    configureSessions()
    //configureSessionAuthentication()
    configureJWTAuthentication(config, httpClient)
    configureSSE()
    configureWebsockets()
    configureRouting(config, httpClient)
    configureSerialization()
    configureStatusPages()
    configureRequestValidation()
    configurePartialContent()
    configureAutoHeadResponse()

    configureShutDownUrl()

    configureCustomHeader()
}
