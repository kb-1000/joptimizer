package de.kb1000.joptimizer.ir.node.adapter

import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.ValueNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelNode

/**
 * Pop a value from the [LowLevelNode] stack to provide it as a [Value]
 */
class AdapterPopNode : AdapterNode(), ValueNode {
    override fun toString() = "@$nodeIndex = adapter_pop"
}