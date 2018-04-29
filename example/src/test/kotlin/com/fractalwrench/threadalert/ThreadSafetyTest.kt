package com.fractalwrench.threadalert

import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.Method
import java.util.*
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
    @Test(expected = ConcurrentModificationException::class)
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

    /**
     * Coerces a property that can be set as null by other threads to non-null
     */
    @Test(expected = NullPointerException::class)
    fun testNpeFail() {
        val example = NpeFail()

        execute {
            for (n in 1..10) {
                example.data = "test"
                example.data!!
            }
        }.verify()
    }

    @Test
    fun testNpePass() {
        val example = NpePass()

        execute {
            for (n in 1..10) {
                example.data = "test"
                example.data!!
            }
        }.verify()
    }


    // TODO below


    /**
     * Asserts that a method is called multiple times due to a lack of locks/semaphores
     */
    @Test
    fun testSemaphoreFail() {
        val handler = MethodCallCountHandler()
        val obj = generateProxiedObject(handler, { it.name == "performFoo" }) as SemaphoreFail

        execute({
            obj.doSomething()
        })
                .completeExecution(false)
                .verify() {
                    Assert.assertEquals(1, handler.count.get())
                }
    }

    /**
     * Asserts that a method is only called once
     */
    @Test
    fun testSemaphorePass() {
        val handler = MethodCallCountHandler()
        val obj = generateProxiedObject(handler, { it.name == "performFoo" }) as SemaphoreFail

        execute {
            obj.doSomething()
        }
                .completeExecution(false)
                .verify {
                    Assert.assertEquals(1, handler.count.get())
                }
    }


    // TODO integrate into lib

    private fun generateProxiedObject(handler: MethodCallCountHandler, methodFilter: (Method) -> Boolean): Any {
        val proxyFactory = ProxyFactory()
        proxyFactory.superclass = SemaphorePass::class.java
        proxyFactory.setFilter(methodFilter) // FIXME

        val create = proxyFactory.create(arrayOf(), arrayOf(), handler)
        return create as SemaphorePass
    }

    class MethodCallCountHandler : MethodHandler {
        val count: AtomicLong = AtomicLong()

        override fun invoke(self: Any?, thisMethod: Method?, proceed: Method?, args: Array<out Any>?): Any {
            count.incrementAndGet()
            return proceed!!.invoke(self) // FIXME only supports non-void methods with no params
        }

    }


}

