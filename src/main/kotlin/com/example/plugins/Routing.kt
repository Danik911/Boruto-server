package com.example.plugins

import com.example.routs.getAllHeroes
import com.example.routs.root
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*

fun Application.configureRouting() {

    routing {
        root()
        getAllHeroes()
        static("/images"){
            resources("images")
        }
    }
}
