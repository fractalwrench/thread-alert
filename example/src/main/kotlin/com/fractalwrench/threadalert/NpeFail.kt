package com.fractalwrench.threadalert

internal class NpeFail {

    var data: String? = "test"
        set(value) {
            field = null
            Thread.sleep(1)
            field = value
    }

}
