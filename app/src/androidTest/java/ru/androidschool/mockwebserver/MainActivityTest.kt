package ru.androidschool.mockwebserver

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.jakewharton.espresso.OkHttp3IdlingResource
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.androidschool.mockwebserver.network.MovieApiClient
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    private val mockWebServer = MockWebServer()
    var rxIdlingResource = RxIdlingResource()

    @Before
    fun setup() {
        mockWebServer.start(8080)
        IdlingRegistry.getInstance().register(
            OkHttp3IdlingResource.create(
                "okhttp",
                MovieApiClient.client
            )
        )
        rxIdlingResource = RxIdlingResource()
        rxIdlingResource.register()
    }

    @Test
    fun testSuccessfulResponse() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    // Формируем усешный ответ от сервера
                    .setResponseCode(200)
                    .setBody(FileReader.readStringFromFile("success_response.json"))
            }

        }

        // Запускаем MainActivity
        activityRule.launchActivity(null)
        // Ждём когда завершится запрос через RxJava
        rxIdlingResource.waitForIdle()

        // Проверяем, что ProgressBar - скрыт
        Espresso.onView(withId(R.id.progress_circular))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(Visibility.GONE)))

        // Проверяем, что список с фильмами виден
        Espresso.onView(withId(R.id.movies_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(Visibility.VISIBLE)))

        // Проверяем, что текст об ошибке - скрыт
        Espresso.onView(withId(R.id.error_text_view))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testFailedResponse() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    // Формируем ошибку
                    .setResponseCode(500)
                    .setBody(FileReader.readStringFromFile("error.json"))
            }
        }

        // Запускаем MainActivity
        activityRule.launchActivity(null)

        // Ждём когда завершится запрос через RxJava
        rxIdlingResource.waitForIdle()

        // Проверяем, что ProgressBar - скрыт
        Espresso.onView(withId(R.id.progress_circular))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        // Проверяем, что список с фильмами скрыт
        Espresso.onView(withId(R.id.movies_recycler_view))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        // Проверяем, что текст об ошибке показан
        Espresso.onView(withId(R.id.error_text_view))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // Проверяем, что текст об ошибке содержит текст "Что-то пошло не так. Попробуйте позже"
        Espresso.onView(withId(R.id.error_text_view))
            .check(matches(withText(R.string.error_title)))
    }


    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
}