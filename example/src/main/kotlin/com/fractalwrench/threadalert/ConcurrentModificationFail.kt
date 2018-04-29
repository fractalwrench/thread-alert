package com.fractalwrench.threadalert

internal class ConcurrentModificationFail {

    private var values = mutableListOf("abc", "def", "ghs")

    fun iterate() {
        values.forEach {
            it.length
        }
    }

    fun addToList(v: String) {
        values.add(v)
    }

}
