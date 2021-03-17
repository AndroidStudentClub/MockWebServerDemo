package ru.androidschool.mockwebserver

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.androidschool.mockwebserver.data.Movie
import ru.androidschool.mockwebserver.network.MovieApiClient
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    val movies_recycler_view by lazy { findViewById<RecyclerView>(R.id.movies_recycler_view) }
    val search_toolbar by lazy { findViewById<SearchBar>(R.id.search_toolbar) }
    val progress_circular by lazy { findViewById<ProgressBar>(R.id.progress_circular) }
    val error_text by lazy { findViewById<TextView>(R.id.error_text_view) }

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
                MovieApiClient.apiClient(application).searchByQuery(
                    MainActivity.API_KEY,
                    "en",
                    it
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setMovies(it.results)
                Log.d(TAG, it.toString())
            }, {
                Log.e(TAG, it.toString())
            })


        // Получаем Single
        val getTopRatedMovies =
            MovieApiClient.apiClient(application).getTopRatedMovies(API_KEY, "ru")

        getTopRatedMovies
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { updateProgress(true) }
            .doFinally { updateProgress(false) }
            .subscribe(
                {
                    val movies = it.results
                    // Передаем результат в adapter и отображаем элементы
                    movies_recycler_view.adapter = MoviesAdapter(movies, R.layout.list_item_movie)
                    movies_recycler_view.visibility = View.VISIBLE
                    updateError()
                },
                { error ->
                    movies_recycler_view.visibility = View.GONE
                    updateError()
                    // Логируем ошибку
                    Log.e(TAG, error.toString())
                }
            )
    }

    private fun updateError() {
        error_text.isVisible = (movies_recycler_view.visibility == View.GONE)
    }

    private fun updateProgress(isVisible: Boolean) {
        progress_circular.isVisible = isVisible
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
