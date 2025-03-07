package de.kb1000.joptimizer.ir.node.constant

import de.kb1000.joptimizer.ir.node.Value

sealed interface Constant : Value {
    override fun valueToString() = "($this)"
    override fun isPure() = true
}
