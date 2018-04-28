package com.fractalwrench.threadalert

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

    val example = SimpleDeadlock()
    val iterations = 1000
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
    println("Count $count")

}
