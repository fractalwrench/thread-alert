package com.fractalwrench.threadalert

internal class ConcurrentModificationFail {

    private var values = mutableListOf("abc", "def", "ghs")

    fun iterate() {
        values.forEach {
            @Suppress("UNNECESSARY_SAFE_CALL")
            it?.length // not thread safe, ignore the non-null warning
        }
    }

    fun addToList(v: String) {
        values.add(v)
    }

}
