package com.example.routs

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*


fun Routing.root() {
    get("/") {
        call.respond(
            status = HttpStatusCode.OK,
            message = "Welcome to Boruto server!"
        )
    }
}