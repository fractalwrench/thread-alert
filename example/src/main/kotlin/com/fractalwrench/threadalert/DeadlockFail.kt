package com.fractalwrench.threadalert

import java.util.concurrent.locks.ReentrantLock

internal class DeadlockFail {

    private val lock = ReentrantLock()

    fun hangForever() {
        lock.lock()
        Thread.currentThread().name.length
    }

}
