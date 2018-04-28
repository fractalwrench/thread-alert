package com.fractalwrench.threadalert

internal class PossibleNpe {

    var data: String? = "test"
        set(value) {
            field = null
            Thread.sleep(1)
            field = value
    }

}
