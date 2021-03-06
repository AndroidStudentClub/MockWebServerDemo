package ru.androidschool.mockwebserver.network

import android.app.Application
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.androidschool.mockwebserver.MockWebServerApp

object MovieApiClient {

    const val BASE_URL = "https://api.themoviedb.org/3/"

    var client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val request = chain.request()
                .newBuilder()
                .build()
            chain.proceed(request)
        })
        .addInterceptor(HttpLoggingInterceptor(CustomHttpLogging()).apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun apiClient(application: Application): MovieApiInterface {

        val retrofit = Retrofit.Builder()
            .baseUrl((application as MockWebServerApp).getBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return retrofit.create(MovieApiInterface::class.java)
    }
}
