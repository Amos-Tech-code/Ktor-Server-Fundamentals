package com.example.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import kotlin.io.path.exists

fun Application.configureRouting() {

    install(RoutingRoot) {
        route("/", HttpMethod.Get) {
            handle {
                call.respondText("Hello Client What do you want to request?")
            }
        }
    }

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

    }

}


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