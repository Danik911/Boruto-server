package com.example.routs

import com.example.models.ApiResponse
import com.example.repository.HeroRepositoryAlternative
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Routing.getAllHeroesAlternative() {

    val heroRepository: HeroRepositoryAlternative by inject()

    get("/boruto/heroes") {
        try {
            val page = call.request.queryParameters["page"]?.toInt() ?: 1
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 4


            val apiResponse = heroRepository.getAllHeroes(page = page, limit = limit)

            call.respond(
                message = apiResponse,
                status = HttpStatusCode.OK
            )
        } catch (e: java.lang.NumberFormatException) {
            call.respond(
                message = ApiResponse(success = false, message = "Invalid request, only numbers are accepted"),
                status = HttpStatusCode.BadRequest
            )
        } catch (e: java.lang.IllegalArgumentException) {
            call.respond(
                message = ApiResponse(success = false, message = "Only numbers from 1 to 5 are accepted"),
                status = HttpStatusCode.NotFound
            )
        }
    }
}