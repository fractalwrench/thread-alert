package com.fractalwrench.threadalert

import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Executes an action concurrently on a large thread pool, thus testing its thread-safety.
 */
fun execute(action: () -> Unit): ThreadAlert = ThreadAlert(action)

class ThreadAlert(private val action: () -> Unit) {

    private val threadPool = Executors.newFixedThreadPool(100)
    private var repeat = 1000
    private var timeout = 100L
    private var completeExecution: Boolean = true

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
    fun verify(): ThreadAlert {
        val latch = CountDownLatch(repeat)
        val throwable = performWork(latch)
        verifyAssertions(latch, throwable)
        return this
    }

    /**
     * Verifies that the action completes without any exceptions, and that each action finished execution.
     *
     * Additionally, custom verification can then take place.
     */
    fun verify(action: () -> Boolean): ThreadAlert {
        verify()
        if (!action()) {
            printFailure("Custom assertion failure")
            throw AssertionError("")
        }
        return this
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

class MethodCallCountHandler : MethodHandler {
    val count: AtomicLong = AtomicLong()

    override fun invoke(self: Any?, thisMethod: Method?, proceed: Method?, args: Array<out Any>?): Any {
        count.incrementAndGet()
        return proceed!!.invoke(self) // FIXME only supports non-void methods with no params
    }
}

/**
 * Generates a proxied object using Javassist to hack the class bytecode at runtime. This allows us
 * to count the number of times a method is called and other custom behaviour.
 */
fun generateProxyObject(handler: MethodCallCountHandler,
                        clz: Class<out Any>,
                        methodFilter: (Method) -> Boolean): Any {
    val proxyFactory = ProxyFactory()
    proxyFactory.superclass = clz
    proxyFactory.setFilter(methodFilter)
    return proxyFactory.create(arrayOf(), arrayOf(), handler)
}

fun calledOnce(handler: MethodCallCountHandler) = 1L == handler.count.get()
