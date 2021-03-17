package ru.androidschool.mockwebserver

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe

class SearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    val search_edit_text: EditText by lazy { findViewById<EditText>(R.id.search_edit_text) }
    val delete_text_button by lazy { findViewById<ImageView>(R.id.delete_text_button) }
    val editText: EditText by lazy { search_edit_text }

    val onTextChangedObservable by lazy {
        Observable.create(ObservableOnSubscribe<String> { subscriber ->
            editText.doAfterTextChanged { text ->
                subscriber.onNext(text.toString())
            }
        })
    }

    private var hint: String = ""
    private var isCancelVisible: Boolean = true

    init {
        LayoutInflater.from(context).inflate(R.layout.search_toolbar, this)
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.SearchBar).apply {
                hint = getString(R.styleable.SearchBar_hint).orEmpty()
                isCancelVisible = getBoolean(R.styleable.SearchBar_cancel_visible, true)
                recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        search_edit_text.hint = hint
        delete_text_button.setOnClickListener {
            search_edit_text.text.clear()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        search_edit_text.afterTextChanged { text ->
            if (!text.isNullOrEmpty() && !delete_text_button.isVisible) {
                delete_text_button.visibility = View.VISIBLE
            }
            if (text.isNullOrEmpty() && delete_text_button.isVisible) {
                delete_text_button.visibility = View.GONE
            }
        }
    }
}

fun EditText.afterTextChanged(action: (s: Editable?) -> Unit) =
    addTextChangedListener(afterTextChanged = action)