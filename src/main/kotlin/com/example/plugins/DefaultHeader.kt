package com.example.plugins

import io.ktor.application.*
import io.ktor.features.*
import java.time.Duration

fun Application.configureDefaultHeader(){
    install(DefaultHeaders){
        val yearInSeconds = Duration.ofDays(365).seconds
        header(
            name = "Cache-control",
            value = "public, max-age=$yearInSeconds, immutable"
        )
    }
}