package com.fractalwrench.threadalert

import java.util.concurrent.locks.ReentrantLock

internal class SimpleDeadlock{

    val lock = ReentrantLock()

    fun hangForever() {
        lock.lock()
        val id = Thread.currentThread().name
        println("Acquired lock on thread $id")
    }

}
