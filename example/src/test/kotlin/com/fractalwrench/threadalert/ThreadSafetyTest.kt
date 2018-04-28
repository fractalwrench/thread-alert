package com.fractalwrench.threadalert

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ThreadSafetyTest {

    val iterations = 1000

    @Test
    fun testDeadlock() {
        val example = SimpleDeadlock()
        val latch = CountDownLatch(iterations)

        for (n in 1..iterations) {
            val r = Runnable {
                example.hangForever()
                println("Completed $n")
                latch.countDown()
            }
            Thread(r).start()
        }

        latch.await(100, TimeUnit.MILLISECONDS)
        val count = latch.count
        Assert.assertEquals(0, count)
    }

    @Test
    fun testConcurrentModification() {
        val example = ConcurrentModification()
        val latch = CountDownLatch(iterations)

        for (n in 1..iterations) {
            val r = Runnable {
                example.addToList("test")
                example.iterate()
                latch.countDown()
            }
            Thread(r).start()
        }

        latch.await(100, TimeUnit.MILLISECONDS)
        val count = latch.count
        Assert.assertEquals(0, count)
    }

    @Test
    fun testNpe() {
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

}

