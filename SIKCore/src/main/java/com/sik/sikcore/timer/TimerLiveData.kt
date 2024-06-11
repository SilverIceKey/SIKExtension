package com.sik.sikcore.timer

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 计时器
 */
class TimerLiveData(
    private val intervalMillis: Long,
    private val millisInFuture: Long,
    private val isCountDown: Boolean = false
) :
    LiveData<Long>() {
    private val scope = MainScope()
    private var job: Job? = null
    private var count: Long = 0

    override fun onActive() {
        super.onActive()
        job = scope.launch {
            while (count < millisInFuture) {
                count += intervalMillis
                if (isCountDown) {
                    postValue(millisInFuture - count)
                } else {
                    postValue(count)
                }
                delay(intervalMillis)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        job?.cancel()
    }
}
