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
            Thread(r).start() // FIXME might use up a few too many resources =)
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

    @Test
    fun testCalledOnce() {
        val latch = CountDownLatch(iterations)
        val handler = MethodCallCountHandler()
        val obj = generateProxiedObject(handler)

        for (n in 1..iterations) {
            val r = Runnable {
                obj.doSomething()
                latch.countDown()
            }
            Thread(r).start()
        }
        latch.await(1500, TimeUnit.MILLISECONDS)
        Assert.assertEquals(1, handler.count.get())

    }

    private fun generateProxiedObject(handler: MethodCallCountHandler): AcquireSemaphore {
        val proxyFactory = ProxyFactory()
        proxyFactory.superclass = AcquireSemaphore::class.java
        proxyFactory.setFilter { it.name == "performFoo" }

        val create = proxyFactory.create(arrayOf(), arrayOf(), handler)
        val obj = create as AcquireSemaphore
        return obj
    }

    class MethodCallCountHandler : MethodHandler {
        val count: AtomicLong = AtomicLong()

        override fun invoke(self: Any?, thisMethod: Method?, proceed: Method?, args: Array<out Any>?): Any {
            count.incrementAndGet()
            return proceed!!.invoke(self) // FIXME only supports non-void methods with no params
        }

    }


}

