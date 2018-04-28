package com.fractalwrench.threadalert

internal class ConcurrentModification {

    var values = mutableListOf("abc", "def", "ghs")

    fun iterate() {
        values.forEach { println(it) }
    }

    fun addToList(v: String) {
        values.add(v)
    }

}
