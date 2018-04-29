package com.fractalwrench.threadalert

import java.util.concurrent.Semaphore

internal open class SemaphorePass {

    private val semaphore = Semaphore(1)

    fun doSomething() {
        if (!semaphore.tryAcquire()) {
            return
        }
        try {
            performFoo()
        } finally {
            semaphore.release()
        }
    }

    open fun performFoo(): String {
        Thread.sleep(1500)
        return "s"
    }

}
