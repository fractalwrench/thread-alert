package com.fractalwrench.threadalert

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun execute(action: () -> Unit): ThreadAlert = ThreadAlert(action)

class ThreadAlert(private val action: () -> Unit) {

    var repeat: Int = 1000
    var timeout: Long = TimeUnit.MILLISECONDS.toMillis(100)

    fun repeat(times: Int): ThreadAlert {
        this.repeat = times
        return this
    }

    fun timeout(time: Long, unit: TimeUnit): ThreadAlert {
        this.timeout = unit.toMillis(time)
        return this
    }

    fun verify() {
        val latch = CountDownLatch(repeat)

        for (n in 0..repeat) {
            val r = Runnable {
                action()
                latch.countDown()
            }
            Thread(r).start()
        }

        latch.await(timeout, TimeUnit.MILLISECONDS)
        val count = latch.count

        if (count != 0L) {
            printFailure("$count runnables did not complete execution. Increase the timeout " +
                    "or investigate crashes or potential deadlocks.")
        }
    }

    fun verify(action: () -> Boolean) {
        verify()
        if (!action()) {
            printFailure("Custom verification method failed.")
            throw AssertionError("")
        }
    }

    private fun printFailure(msg: String, e: Throwable? = null) {
        val message = "\nThread-Alert Failure:\n$msg"
        println(message)
        throw AssertionError(message, e)

    }

}
