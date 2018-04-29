package com.fractalwrench.threadalert

import org.junit.Test
import java.util.*


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

    /**
     * Asserts that a method is called multiple times due to a lack of locks/semaphores
     */
    @Test(expected = AssertionError::class)
    fun testSemaphoreFail() {
        val handler = MethodCallCountHandler()
        val obj = generateProxyObject(handler, SemaphoreFail::class.java, {
            it.name == "performFoo"
        }) as SemaphoreFail

        execute { obj.doSomething() }
                .completeExecution(false)
                .verify { calledOnce(handler) }
    }

    /**
     * Asserts that a method is only called once
     */
    @Test
    fun testSemaphorePass() {
        val handler = MethodCallCountHandler()
        val obj = generateProxyObject(handler, SemaphorePass::class.java, {
            it.name == "performFoo"
        }) as SemaphorePass

        execute { obj.doSomething() }
                .completeExecution(false)
                .verify { calledOnce(handler) }
    }

}
