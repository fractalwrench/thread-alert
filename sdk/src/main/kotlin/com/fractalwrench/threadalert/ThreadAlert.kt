package com.fractalwrench.threadalert

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Executes an action concurrently on a large thread pool, thus testing its thread-safety.
 */
fun execute(action: () -> Unit): ThreadAlert = ThreadAlert(action)

class ThreadAlert(private val action: () -> Unit) {

    private var repeat = 1000
    private var timeout = 100L
    private var completeExecution: Boolean = true
    private val threadPool = Executors.newFixedThreadPool(100)

    /**
     * Configures the amount of times the action should be repeated (1000 by default)
     */
    fun repeat(times: Int): ThreadAlert {
        this.repeat = times
        return this
    }

    /**
     * Configures the amount of time to wait for execution to complete (100ms by default)
     */
    fun timeout(ms: Long): ThreadAlert {
        this.timeout = ms
        return this
    }

    /**
     * Configures whether or not it should be mandatory for all actions to complete execution (true by default)
     */
    fun completeExecution(completeExecution: Boolean): ThreadAlert {
        this.completeExecution = completeExecution
        return this
    }

    /**
     * Verifies that the action completes without any exceptions, and that each action finished execution.
     */
    fun verify() {
        val latch = CountDownLatch(repeat)
        val throwable = performWork(latch)
        verifyAssertions(latch, throwable)
    }

    /**
     * Verifies that the action completes without any exceptions, and that each action finished execution.
     *
     * Additionally, custom verification can then take place.
     */
    fun verify(action: () -> Unit) {
        verify()
        action()
    }

    private fun performWork(latch: CountDownLatch): Throwable? {
        var throwable: Throwable? = null

        for (n in 1..repeat) {
            threadPool.submit({
                try {
                    action()
                } catch (e: Throwable) {
                    throwable = e
                }
                latch.countDown()
            })
        }
        latch.await(timeout, TimeUnit.MILLISECONDS)
        return throwable
    }

    private fun verifyAssertions(latch: CountDownLatch, throwable: Throwable?) {
        val count = latch.count

        if (throwable != null) {
            printFailure("Encountered at least one exception in execution." +
                    " Showing most recent stacktrace")
            println(throwable)
            throw throwable
        } else if (count != 0L && completeExecution) {
            printFailure("$count runnables did not complete execution. Increase the timeout " +
                    "or investigate crashes or potential deadlocks.")
            throw AssertionError("")
        }
    }

    private fun printFailure(msg: String) {
        val message = "\nThread-Alert Failure:\n$msg"
        println(message)
    }

}
