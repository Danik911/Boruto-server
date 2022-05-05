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
                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString())
                val expectedResult = ApiResponse(
                    success = true,
                    message = "OK",
                    nextPage = 2,
                    heroes = heroesRepository.page1,
                    lastUpdate = actualResult.lastUpdate
                )

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
                    val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString())
                    val expectedResult = ApiResponse(
                        success = true,
                        message = "OK",
                        prevPage = calculatePage(page)["prevPage"],
                        nextPage = calculatePage(page)["nextPage"],
                        heroes = heroes[page - 1],
                        lastUpdate = actualResult.lastUpdate
                    )

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
    @Test
    fun `search one hero, expect expect one hero found`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=sas").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )

                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString()).heroes.size

                assertEquals(
                    expected = 1,
                    actual = actualResult
                )
            }
        }
    }
    @Test
    fun `search heroes, expect expect multiple heroes found`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=sa").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )

                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString()).heroes.size

                assertEquals(
                    expected = 3,
                    actual = actualResult
                )
            }
        }
    }
    @Test
    fun `search unknown hero, expect expect empty list found`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=unknown").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )

                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString()).heroes

                assertEquals(
                    expected = emptyList(),
                    actual = actualResult
                )
            }
        }
    }
    @Test
    fun `search empty search field, expect empty list found`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )

                val actualResult = Json.decodeFromString<ApiResponse>(response.content.toString()).heroes

                assertEquals(
                    expected = emptyList(),
                    actual = actualResult
                )
            }
        }
    }
    @Test
    fun `get non existing end point, expect an error`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/unknown").apply {
                assertEquals(
                    expected = HttpStatusCode.NotFound,
                    actual = response.status()
                )

                assertEquals(
                    expected = "Page not found",
                    actual = response.content
                )
            }
        }
    }

    private fun calculatePage(page: Int) =
        mapOf<String, Int?>(
            PREV_PAGE_KEY to if (page in 2..5) page.minus(1) else null,
            NEXT_PAGE_KEY to if (page in 1..4) page.plus(1) else null

        )
}