package com.fractalwrench.threadalert

import java.util.concurrent.Semaphore

open internal class AcquireSemaphore {

    val semaphore = Semaphore(1)

    fun doSomething() {
//        if (!semaphore.tryAcquire()) {
//            return
//        }
//        try {
            performFoo()
//        } finally {
//            semaphore.release()
//        }
    }

    open fun performFoo(): String {
        println("Foo")
        Thread.sleep(1000)
        return "s"
    }

}
