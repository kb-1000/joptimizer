package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.constant.IntConstant

class LowLevelIntConstNode(var value: Int) : LowLevelNode(), LowLevelNoSideEffectsConstNode {
    override val size = 1

    override fun toString() = "ll_int $value"

    override fun asConstant() = IntConstant(value)
}
