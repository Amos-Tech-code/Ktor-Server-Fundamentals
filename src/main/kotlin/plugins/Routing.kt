package com.example.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream

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

        post("/status") {
            //throw Exception("Database failed to initialize")
            call.respond(HttpStatusCode.Unauthorized)
        }

    }

}


@Serializable
data class Product(
    val name: String,
    val category: String,
    val price: Int
)