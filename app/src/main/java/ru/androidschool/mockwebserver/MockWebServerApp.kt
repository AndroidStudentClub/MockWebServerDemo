package ru.androidschool.mockwebserver

import android.app.Application
import ru.androidschool.mockwebserver.network.MovieApiClient

open class MockWebServerApp : Application() {
    open fun getBaseUrl() = MovieApiClient.BASE_URL
}