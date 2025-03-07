package de.kb1000.joptimizer.ir.node.adapter

import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.ValueNode
import de.kb1000.joptimizer.ir.node.adapter.AdapterMergeLongValueNode.Kind
import org.jetbrains.annotations.Range

class AdapterSplitLongValueNode(var kind: Kind, var part: @Range(from = 0, to = 1) Byte, var value: Value) : AdapterNode(), ValueNode {
    override fun toString() = "@$nodeIndex = adapter_split_long ${kind.name.lowercase()} part $part of ${value.valueToString()}"

    override fun getUse(int: Int) = when (int) {
        0 -> value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> this.value = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 1

    override fun isPure() = true
}
