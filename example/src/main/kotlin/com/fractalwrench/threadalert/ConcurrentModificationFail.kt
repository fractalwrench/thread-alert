package com.fractalwrench.threadalert

internal class ConcurrentModificationFail {

    var values = mutableListOf("abc", "def", "ghs")

    fun iterate() {
        values.forEach { it.length }
    }

    fun addToList(v: String) {
        values.add(v)
    }

}
