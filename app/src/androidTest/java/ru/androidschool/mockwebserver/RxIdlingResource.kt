package ru.androidschool.mockwebserver


import android.util.Log
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import io.reactivex.functions.Function
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantReadWriteLock

class RxIdlingResource : IdlingResource,
    Function<Runnable, Runnable> {
    // Guarded by IDLING_STATE_LOCK
    private var taskCount = 0

    // Guarded by IDLING_STATE_LOCK
    private var transitionCallback: ResourceCallback? = null
    override fun getName(): String {
        return TAG
    }

    override fun isIdleNow(): Boolean {
        val result: Boolean
        IDLING_STATE_LOCK.readLock().lock()
        result = taskCount == 0
        IDLING_STATE_LOCK.readLock().unlock()
        return result
    }

    override fun registerIdleTransitionCallback(callback: ResourceCallback) {
        IDLING_STATE_LOCK.writeLock().lock()
        transitionCallback = callback
        IDLING_STATE_LOCK.writeLock().unlock()
    }

    @Throws(Exception::class)
    override fun apply(runnable: Runnable): Runnable {
        IDLING_STATE_LOCK.writeLock().lock()
        taskCount++
        Log.d(TAG, "TaskCount increase $taskCount")
        IDLING_STATE_LOCK.writeLock().unlock()
        return Runnable {
            try {
                runnable.run()
            } finally {
                IDLING_STATE_LOCK.writeLock().lock()
                try {
                    taskCount--
                    Log.d(TAG, "TaskCount decrease $taskCount")
                    if (taskCount == 0 && transitionCallback != null) {
                        transitionCallback!!.onTransitionToIdle()
                        Log.d(TAG, "idle ")
                    }
                } finally {
                    IDLING_STATE_LOCK.writeLock().unlock()
                }
            }
        }
    }

    fun waitForIdle() {
        if (!isIdleNow) {
            val latch = CountDownLatch(1)
            registerIdleTransitionCallback { latch.countDown() }
            try {
                latch.await()
            } catch (e: InterruptedException) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    fun register() {
        RxJavaPlugins.setScheduleHandler(this)
    }

    companion object {
        private val TAG = RxIdlingResource::class.java.simpleName
        private val IDLING_STATE_LOCK =
            ReentrantReadWriteLock()
    }
}