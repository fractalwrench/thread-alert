package com.fractalwrench.threadalert

internal open class SemaphoreFail {

    fun doSomething() = performFoo()

    open fun performFoo(): String {
        Thread.sleep(1000)
        return "s"
    }

}
