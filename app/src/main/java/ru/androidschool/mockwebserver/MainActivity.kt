package ru.androidschool.mockwebserver

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.androidschool.mockwebserver.data.Movie
import ru.androidschool.mockwebserver.network.MovieApiClient
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    val movies_recycler_view by lazy { findViewById<RecyclerView>(R.id.movies_recycler_view) }
    val search_toolbar by lazy { findViewById<SearchBar>(R.id.search_toolbar) }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_toolbar
            .onTextChangedObservable
            .map { it.trim() }
            .debounce(500, TimeUnit.MILLISECONDS)
            .filter { it.isNotEmpty() }
            .observeOn(Schedulers.io())
            .flatMapSingle { it ->
                MovieApiClient.apiClient.searchByQuery(
                    MainActivity.API_KEY,
                    "en",
                    it
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setMovies(it.results)
                Log.d(MainActivity.TAG, it.toString())
            }, {
                Log.e(MainActivity.TAG, it.toString())
            })


        // Получаем Single
        val getTopRatedMovies = MovieApiClient.apiClient.getTopRatedMovies(API_KEY, "ru")

        getTopRatedMovies
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val movies = it.results
                    // Передаем результат в adapter и отображаем элементы
                    movies_recycler_view.adapter = MoviesAdapter(movies, R.layout.list_item_movie)
                },
                { error ->
                    // Логируем ошибку
                    Log.e(TAG, error.toString())
                }
            )
    }

    fun setMovies(movies: List<Movie>) {
        movies_recycler_view.adapter = MoviesAdapter(movies, R.layout.list_item_movie)
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName

        // TODO - insert your themoviedb.org API KEY here
        private val API_KEY = ""
    }
}
