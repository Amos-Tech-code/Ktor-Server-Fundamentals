package com.example.plugins

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import io.ktor.server.sessions.*
import io.ktor.server.sse.*
import io.ktor.server.websocket.*
import io.ktor.sse.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists

fun Application.configureRouting(
    config: JWTConfig,
    httpClient: HttpClient
) {

    val usersDB = mutableMapOf<String, String>()
    val userDB_2 = mutableMapOf<String, UserInfo>()

//    install(RoutingRoot) {
//        route("/", HttpMethod.Get) {
//            handle {
//                call.respondText("Hello Client What do you want to request?")
//            }
//        }
//    }

    routing {

        post("/channel") {
            val channel = call.receiveChannel()
            val text = channel.readRemaining().readText()

            call.respondText(text)
        }

        post("/upload") {
            val file = File("uploads/sample2.jpg").apply {
                parentFile?.mkdirs()
            }
//            val byteArray = call.receive<ByteArray>()
//            file.writeBytes(byteArray)
//
            val channel = call.receiveChannel()
            channel.copyAndClose(file.writeChannel())
            call.respond("File upload successful.")
        }

        post("/product") {
            val product = call.receiveNullable<Product>()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            call.respond(product)
        }

        post("/checkout") {
            val formData = call.receiveParameters()
            val productId = formData["productId"]
            val quantity = formData["quantity"]
            call.respondText("Order Placed Successfully.Product Id: $productId Quantity: $quantity")
        }

        post("/multipartData") {

            val data = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 50)
            val fields = mutableMapOf<String, MutableList<String>>()

            data.forEachPart { part->
                when(part) {
                    is PartData.FileItem -> {
                        val key = part.name ?: return@forEachPart
                        val fileName = part.originalFileName ?: return@forEachPart
                        fields.getOrPut(key) { mutableListOf() }.add(fileName)

                        val file = File("uploads/$fileName").apply {
                            parentFile?.mkdirs()
                        }
                        part.provider().copyAndClose(file.writeChannel())
                        part.dispose()
                    }
                    is PartData.FormItem -> {
                        val key = part.name ?: return@forEachPart
                        fields.getOrPut(key){ mutableListOf() }.add(part.value)
                        part.dispose()
                    }
                    else -> {}
                }
            }

            call.respond("Form fields: $fields")
        }

        post("/statusTest") {
            //throw Exception("Database failed to initialize")
            call.respond(HttpStatusCode.Unauthorized)
        }

        post("/requestValidation") {
            val message = call.receive<String>()
            call.respond(message)
        }

        post("productValidation") {
            val product = call.receive<Product>()
            call.respond(product)
        }

        //Validation Impl
        route("message1") {

            install(RequestValidation) {
                validate<String> { body ->
                    if (body.isBlank()) {
                        ValidationResult.Invalid("Message cannot be empty")
                    } else if (!body.startsWith("Hello")) {
                        ValidationResult.Invalid("Invalid Message")
                    } else {
                        ValidationResult.Valid
                    }
                }
            }
            post {
                val message = call.receive<String>()
                call.respond(message)
            }
        }
        //Rate Limit Impl
        post("limit") {
            val requestsLeft = call.response.headers["X-RateLimit-Remaining"]
            call.respondText("$requestsLeft requests left.")
        }

        rateLimit(RateLimitName("public")) {
            post("limitpublic") {
                val requestsLeft = call.response.headers["X-RateLimit-Remaining"]
                call.respondText("$requestsLeft requests left.")
            }
        }

        rateLimit(RateLimitName("protected")) {
            post("protected") {
                val requestsLeft = call.response.headers["X-RateLimit-Remaining"]
                call.respondText("$requestsLeft requests left.")
            }

        }

        //Json Response
        get("products") {

            val response = ProductResponse(
                success = true,
                message = "Successfully fetched products",
                data = List(10) {
                    Product(name = "Orange", "Fruits", 10)
                }
            )
            call.respond(response)

        }
        //Stream
        get("stream") {
            val fileName = call.request.queryParameters["fileName"] ?: ""
            val file = File("uploads/$fileName")
            if (!file.exists()) return@get call.respond(HttpStatusCode.NotFound)

            call.respondFile(file)
        }
        //Download
        get("download") {
            val fileName = call.request.queryParameters["fileName"] ?: ""
            val file = File("uploads/$fileName")
            if (!file.exists()) return@get call.respond(HttpStatusCode.NotFound)

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    fileName
                ).toString()
            )
            call.respondFile(file)
        }

        //Path of the file
        get("fileFromPath") {
            val fileName = call.request.queryParameters["fileName"] ?: ""
            val filePath = Path.of("uploads/$fileName")

            if (!filePath.exists()) return@get call.respond(HttpStatusCode.NotFound)

            call.respond(LocalPathContent(filePath))

        }

        //Custom Error Status
        get("customStatus") {
            call.response.status(HttpStatusCode(413, "Custom Error Status"))
        }
        //Headers
        get("headers") {
            call.response.headers.append(
                HttpHeaders.ETag, "wertyudghqsc"
            )
            //Alternatively
            call.response.header(HttpHeaders.ETag, "12343542")
            //Alternatively
            call.response.etag("qwerty")
            //Custom Header
            call.response.header(
                "Custom-Header",
                "My Value"
            )

            call.respond(HttpStatusCode.OK)
        }

        //Cookies
        get("cookies") {
            call.response.cookies.append(
                "Cookie", "Cookie Value"
            )
            call.respond(HttpStatusCode.OK)
        }
        //Redirect
        get("redirect") {
            call.respondRedirect("moved", permanent = true)
        }

        get("moved") {
            call.respondText("Redirected to moved route")
        }
        //Static
        staticResources("/static", "static") {
            extensions("html")
        }
        //Static files
        staticFiles("/uploads", File("uploads")) {
            exclude { file ->
                file.path.contains("jpeg") //Exclude files
            }

            contentType { file ->
                when (file.name) {
                    "index.txt" -> ContentType.Text.Html
                    else -> null
                }
            }

            cacheControl { file ->
                when (file.name) {
                    "index.txt" -> listOf(CacheControl.MaxAge(10000))
                    else -> emptyList()
                }

               // listOf(CacheControl.MaxAge(10000))
            }
        }
        //Static Zip Files
        staticZip("/zips", "uploads", zip = Paths.get("zips/uploads.zip"))

        //Authentication -> Session
//        authenticate("session-auth") {
//            get("auth") {
//                val username = call.principal<UserSession>()?.userName
//                call.respond("Hello $username!")
//            }
//        }

        //JWT Authentication
        authenticate("jwt-auth") {
            get("") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                //val expiresAt = principal?.expiresAt?.time?.minus(System.currentTimeMillis())
                val userInfo = userDB_2[username] ?: mapOf("error" to true)
                call.respond(userInfo)

                //call.respond("Hello $username! The token expires after $expiresAt milliseconds")
            }
        }
        //Auth sessions
        post("signup") {
            val requestData = call.receive<AuthRequest>()

            if (usersDB.contains(requestData.userName)) {
                call.respondText("User already exists")
            } else {
                usersDB[requestData.userName] = requestData.password
                //call.sessions.set(UserSession(requestData.userName))
                val token = generateToken(config = config, userName = requestData.userName)
                call.respond(mapOf("token" to token, "message" to "User signup success"))
            }
        }

        post("login") {
            val requestData = call.receive<AuthRequest>()
            val storedPassword = usersDB[requestData.userName] ?: return@post call.respondText("User doesn't exist")

            if (storedPassword == requestData.password) {
                //call.sessions.set(UserSession(requestData.userName))
                val token = generateToken(config = config, userName = requestData.userName)
                call.respond(mapOf("token" to token, "message" to "Login success"))
            } else {
                call.respondText("Invalid Credentials")
            }

        }

        //Google Login
        authenticate("google-oauth") {

            get("login-google") {

            }

            get("callback") {
                val principal : OAuthAccessTokenResponse.OAuth2? = call.principal()
                if (principal == null) {
                    call.respondText("OAUTH failed", status = HttpStatusCode.Unauthorized)
                    return@get
                }

                val accessToken = principal.accessToken
                val userInfo = fetchGoogleUserInfo(
                    httpClient = httpClient,
                    accessToken = accessToken
                )

                if (userInfo != null) {
                    userDB_2[userInfo.userId] = userInfo
                    val token = generateToken(config, userName = userInfo.userId)
                    call.respond(mapOf("token" to token))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }

            }
        }

        post("logout") {
//            call.sessions.clear<UserSession>()
//            call.respondText("Logout success")
        }

        //Server Sent Events
        sse("events") {
            repeat(6) {
                send(ServerSentEvent("Event: ${it + 1}"))
                delay(1000L)
            }
        }

        //Websockets
        val onlineUsers = ConcurrentHashMap<String, WebSocketSession>()

        webSocket("chat") {
            val userName = call.request.queryParameters["userName"] ?: run {
                this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "UserName is required for connection establishment."))
                return@webSocket
            }

            onlineUsers[userName] = this
            send("You are connected!")
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val message = Json.decodeFromString<Message>(frame.readText())
                        if (message.to.isNullOrBlank()) {
                            onlineUsers.values.forEach {
                                it.send("$userName : ${message.text}")
                            }
                        } else {
                            val session = onlineUsers[message.to]

                            session?.send("$userName : ${message.text}")
                        }
                    }
                }

            } finally {
                onlineUsers.remove(userName)
                this.close()

            }
        }

        //Testing Call Logging
        get("hello") {

            call.respondText("Hello World!")
        }

        get("hi"){
            call.respondText("Hello world!!")
        }

    }


}

@Serializable
data class AuthRequest(
    val userName: String,
    val password: String
)

@Serializable
data class ProductResponse(
    val success: Boolean,
    val message: String,
    val data: List<Product>
)

@Serializable
data class Product(
    val name: String?,
    val category: String?,
    val price: Int?
)

@Serializable
data class Message(
    val text: String,
    val to: String? = null,

)