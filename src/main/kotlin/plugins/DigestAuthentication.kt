package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import java.security.MessageDigest

const val Realm = "Access protected routes"


val userTable : Map<String,ByteArray> = mapOf(
    "admin" to getMD5Digest("admin:${Realm}:password"),
    "user" to getMD5Digest("user:${Realm}:123")
)


fun getMD5Digest(value:String):ByteArray {
    return MessageDigest
        .getInstance("MD5")
        .digest(value.toByteArray())
}

fun Application.configureDigestAuthentication(){

    install(Authentication){

        digest("digest-auth") {

            realm = Realm

            digestProvider { username, realm ->
                userTable[username]
            }

            validate { credentials ->
                if (credentials.userName.isNotBlank()){
                    UserIdPrincipal(credentials.userName)
                }else{
                    null
                }
            }


        }

    }


}