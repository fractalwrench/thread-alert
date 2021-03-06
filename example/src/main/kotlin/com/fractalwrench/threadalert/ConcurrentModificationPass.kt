package com.fractalwrench.threadalert

import java.util.concurrent.ConcurrentLinkedQueue

internal class ConcurrentModificationPass {

    private var values = ConcurrentLinkedQueue<String>()

    fun iterate() {
        values.forEach { it.length }
    }

    fun addToList(v: String) {
        values.add(v)
    }

}
