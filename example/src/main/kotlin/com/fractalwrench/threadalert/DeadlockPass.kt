package com.fractalwrench.threadalert

import java.util.concurrent.locks.ReentrantLock

internal class DeadlockPass{

    private val lock = ReentrantLock()

    fun hangForever() {
        lock.lock()
        try {
            Thread.currentThread().name.length
        } finally {
            lock.unlock()
        }
    }

}
