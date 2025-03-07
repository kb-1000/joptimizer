package de.kb1000.joptimizer.util

import java.util.*
import java.util.stream.Stream

inline fun <reified T> Array<T>.stream(): Stream<T> = Arrays.stream(this)
