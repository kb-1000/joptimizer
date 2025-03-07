package de.kb1000.joptimizer.ir.node

interface Value {
    override fun toString(): String

    fun valueToString(): String

    fun isPure() = false
}
