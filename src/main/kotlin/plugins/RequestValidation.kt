package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {

    install(RequestValidation) {

//        validate<String> { body ->
//            if (body.isBlank()) {
//                ValidationResult.Invalid("Message cannot be empty")
//            } else if (!body.startsWith("Hello")) {
//                ValidationResult.Invalid("Invalid Message")
//            } else {
//                ValidationResult.Valid
//            }
//        }

        validate<Product> { body ->
            if (body.name.isNullOrBlank()) {
                ValidationResult.Invalid("Invalid Product Name")
            } else if (body.category.isNullOrBlank()) {
                ValidationResult.Invalid("Invalid Product Category")
            } else if (body.price == null || body.price <= 0 ) {
                ValidationResult.Invalid("Invalid Product Price")
            } else {
                ValidationResult.Valid
            }
        }
    }
}