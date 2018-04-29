package com.fractalwrench.threadalert

import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong


class ThreadSafetyTest {

    /**
     * Acquires a lock and never unlocks it
     */
    @Test(expected = AssertionError::class)
    fun testDeadlockFail() {
        val deadlockExample = DeadlockFail()

        execute(deadlockExample::hangForever)
                .repeat(100) // avoid claiming all the system resources
                .verify()
    }

    /**
     * Acquires a lock then unlocks it
     */
    @Test
    fun testDeadlockPass() {
        val deadlockExample = DeadlockPass()

        execute(deadlockExample::hangForever)
                .repeat(100) // avoid claiming all the system resources
                .verify()
    }

    /**
     * Modifies a non-thread safe collection while an iterator is in use
     */
    @Test(expected = AssertionError::class)
    fun testConcurrentModificationFail() {
        val example = ConcurrentModificationFail()

        execute {
            example.addToList("test")
            example.iterate()
        }.verify()
    }

    /**
     * Modifies a thread safe collection while an iterator is in use
     */
    @Test
    fun testConcurrentModificationPass() {
        val example = ConcurrentModificationPass()

        execute {
            example.addToList("test")
            example.iterate()
        }.verify()
    }



    // TODO below


    @Test
    fun testNpe() {
        val iterations = 5000 // number of iterations
        val example = PossibleNpe()
        val latch = CountDownLatch(iterations)

        for (n in 1..iterations) {
            val r = Runnable {
                example.data = "test"
                example.data!!
                latch.countDown()
            }
            Thread(r).start()
        }

        latch.await(100, TimeUnit.MILLISECONDS)
        val count = latch.count
        Assert.assertEquals(0, count)
    }



    /**
     * Asserts that a method is called multiple times due to a lack of locks/semaphores
     */
    @Test
    fun testSemaphoreFail() {
        val handler = MethodCallCountHandler()
        val obj = generateProxiedObject(handler)

        execute {
            obj.doSomething()
        }
                .timeout(1500, TimeUnit.MILLISECONDS)
                .verify { handler.count.get() == 1L } // TODO assert to get error message?
    }

    /**
     * Asserts that a method is only called once
     */
    @Test
    fun testSemaphorePass() {
        val handler = MethodCallCountHandler()
        val obj = generateProxiedObject(handler)

        execute {
            obj.doSomething()
        }
                .timeout(1500, TimeUnit.MILLISECONDS)
                .verify { handler.count.get() == 1L } // TODO assert to get error message?
    }



    // TODO integrate into lib

    private fun generateProxiedObject(handler: MethodCallCountHandler): SemaphoreFail {
        val proxyFactory = ProxyFactory()
        proxyFactory.superclass = SemaphoreFail::class.java
        proxyFactory.setFilter { it.name == "performFoo" }

        val create = proxyFactory.create(arrayOf(), arrayOf(), handler)
        return create as SemaphoreFail
    }

    class MethodCallCountHandler : MethodHandler {
        val count: AtomicLong = AtomicLong()

        override fun invoke(self: Any?, thisMethod: Method?, proceed: Method?, args: Array<out Any>?): Any {
            count.incrementAndGet()
            return proceed!!.invoke(self) // FIXME only supports non-void methods with no params
        }

    }


}

