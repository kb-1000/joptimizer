package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.constant.LongConstant

class LowLevelLongConstNode(var value: Long) : LowLevelNode(), LowLevelNoSideEffectsConstNode {
    override val size: Int
        get() = 2

    override fun toString() = "ll_long $value"

    override fun asConstant() = LongConstant(value)
}
