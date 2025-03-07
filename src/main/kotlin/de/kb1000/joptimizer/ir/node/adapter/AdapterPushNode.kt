package de.kb1000.joptimizer.ir.node.adapter

import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelNode

/**
 * Push a [Value] to the [LowLevelNode] stack
 */
class AdapterPushNode(var value: Value) : AdapterNode() {
    override fun toString() = "adapter_push ${value.valueToString()}"

    override fun getUse(int: Int) = when (int) {
        0 -> value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> this.value = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 1
}
