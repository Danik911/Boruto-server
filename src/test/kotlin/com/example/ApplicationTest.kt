package com.example

import com.example.models.ApiResponse
import com.example.models.Hero
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.application.*
import io.ktor.response.*
import kotlin.test.*
import io.ktor.server.testing.*
import com.example.plugins.*
import com.example.repository.HeroRepository
import com.example.repository.NEXT_PAGE_KEY
import com.example.repository.PREV_PAGE_KEY
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject

class ApplicationTest {
    @Test
    fun `get to the root, expect result is equal`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )
                assertEquals(
                    expected = "Welcome to Boruto server!",
                    actual = response.content
                )
            }
        }
    }

    @Test
    fun `get all heroes default page, expect result is equal heroes from page 1`() {
        withTestApplication(moduleFunction = Application::module) {

            val heroesRepository: HeroRepository by inject(HeroRepository::class.java)

            handleRequest(HttpMethod.Get, "/boruto/heroes").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )
                val expectedResult = ApiResponse(
                    success = true,
                    message = "OK",
                    nextPage = 2,
                    heroes = heroesRepository.page1
                )
                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString())
                println("$expectedResult")
                println("$actualResult")
                assertEquals(
                    expected = expectedResult,
                    actual = actualResult
                )
            }
        }
    }

    @Test
    fun `get all heroes from all pages sequentially, expected result is equal`() {
        withTestApplication(moduleFunction = Application::module) {

            val heroesRepository: HeroRepository by inject(HeroRepository::class.java)

            val pages = (1..5)
            val heroes = listOf(
                heroesRepository.page1,
                heroesRepository.page2,
                heroesRepository.page3,
                heroesRepository.page4,
                heroesRepository.page5
            )
            pages.forEach { page ->
                handleRequest(HttpMethod.Get, "/boruto/heroes?page=$page").apply {
                    println("PAGE: $page")
                    assertEquals(
                        expected = HttpStatusCode.OK,
                        actual = response.status()
                    )
                    val expectedResult = ApiResponse(
                        success = true,
                        message = "OK",
                        prevPage = calculatePage(page)["prevPage"],
                        nextPage = calculatePage(page)["nextPage"],
                        heroes = heroes[page - 1]
                    )
                    val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString())
                    println("$expectedResult")
                    println("$actualResult")
                    assertEquals(
                        expected = expectedResult,
                        actual = actualResult
                    )
                }

            }


        }

    }
    @Test
    fun `get all heroes page number out of the range, expect error`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes?page=6").apply {
                assertEquals(
                    expected = HttpStatusCode.NotFound,
                    actual = response.status()
                )
                val expectedResult = ApiResponse(
                    success = false,
                    message = "Only numbers from 1 to 5 are accepted",

                )
                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString())
                println("$expectedResult")
                println("$actualResult")
                assertEquals(
                    expected = expectedResult,
                    actual = actualResult
                )
            }
        }
    }
    @Test
    fun `get all heroes invalid page number, expect error`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes?page=invalid").apply {
                assertEquals(
                    expected = HttpStatusCode.BadRequest,
                    actual = response.status()
                )
                val expectedResult = ApiResponse(
                    success = false,
                    message = "Invalid request, only numbers are accepted",

                    )
                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString())
                println("$expectedResult")
                println("$actualResult")
                assertEquals(
                    expected = expectedResult,
                    actual = actualResult
                )
            }
        }
    }

    private fun calculatePage(page: Int): Map<String, Int?> {
        var prevPage: Int? = page
        var nextPage: Int? = page

        if (page in 2..5) {
            prevPage = prevPage?.minus(1)
        }
        if (page in 1..4) {
            nextPage = nextPage?.plus(1)
        }
        if (page == 5) {
            nextPage = null
        }
        if (page == 1) {
            prevPage = null
        }
        return mapOf(PREV_PAGE_KEY to prevPage, NEXT_PAGE_KEY to nextPage)
    }
}