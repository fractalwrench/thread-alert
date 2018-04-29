package com.fractalwrench.threadalert

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun execute(action: () -> Unit): ThreadAlert = ThreadAlert(action)

class ThreadAlert(private val action: () -> Unit) {

    private var repeat = 1000
    private var timeout = 100L

    fun repeat(times: Int): ThreadAlert {
        this.repeat = times
        return this
    }

    fun timeout(ms: Long): ThreadAlert {
        this.timeout = ms
        return this
    }

    fun verify() {
        val latch = CountDownLatch(repeat)
        var throwable: Throwable? = null

        for (n in 0..repeat) {
            val r = Runnable {
                try {
                    action()
                } catch (e: Throwable) {
                    throwable = e
                }
                latch.countDown()
            }
            Thread(r).start()
        }

        latch.await(timeout, TimeUnit.MILLISECONDS)
        val count = latch.count

        if (throwable != null) {
            printFailure("Encountered at least one exception in execution." +
                    " Showing most recent stacktrace")
            println(throwable)
            throw throwable as Throwable
        } else if (count != 0L) {
            printFailure("$count runnables did not complete execution. Increase the timeout " +
                    "or investigate crashes or potential deadlocks.")
            throw AssertionError("")
        }
    }

    fun verify(action: () -> Boolean) {
        verify()
        if (!action()) {
            printFailure("Custom verification method failed.")
            throw AssertionError("")
        }
    }

    private fun printFailure(msg: String) {
        val message = "\nThread-Alert Failure:\n$msg"
        println(message)
    }

}
