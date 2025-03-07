package de.kb1000.joptimizer.ir.node.adapter

import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.ValueNode

/**
 * Merges a double or long value from its two parts
 */
class AdapterMergeLongValueNode(var kind: Kind, var part1: Value, var part2: Value) : AdapterNode(), ValueNode {
    enum class Kind {
        LONG, DOUBLE
    }

    override fun toString() = "@$nodeIndex = adapter_merge_long ${kind.name.lowercase()} ${part1.valueToString()} ${part2.valueToString()}"

    override fun getUse(int: Int) = when (int) {
        0 -> part1
        1 -> part2
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> part1 = value
        1 -> part2 = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 2

    override fun isPure() = true
}
