package de.kb1000.joptimizer.util

inline fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, crossinline function: () -> V): V =
    computeIfAbsent(key) { function() }
