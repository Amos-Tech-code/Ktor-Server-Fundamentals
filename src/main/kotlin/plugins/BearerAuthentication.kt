package com.example.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer

val usersDb : Map<String,String> = mapOf(
    "token1" to "user1",
    "token2" to "user2",
    "token3" to "user3",
    "token4" to "user4",
)

fun Application.configureBearerAuthentication(){
    install(Authentication){
        bearer("bearer-auth") {
            realm = "Access protected routes"
            authenticate { tokenCredential ->
                val user = usersDb[tokenCredential.token]
                if (!user.isNullOrBlank()){
                    UserIdPrincipal(user)
                }else{
                    null
                }
            }
        }
    }
}