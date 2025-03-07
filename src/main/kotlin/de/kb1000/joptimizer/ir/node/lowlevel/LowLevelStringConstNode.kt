package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.constant.StringConstant

class LowLevelStringConstNode(var value: String) : LowLevelNode(), LowLevelNoSideEffectsConstNode {
    override val size = 1

    override fun toString() = "ll_string \"$value\""

    override fun asConstant() = StringConstant(value)
}
