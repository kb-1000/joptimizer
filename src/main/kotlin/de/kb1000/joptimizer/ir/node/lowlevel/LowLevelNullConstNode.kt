package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.constant.NullConstant

class LowLevelNullConstNode : LowLevelNode(), LowLevelNoSideEffectsConstNode {
    override val size = 1

    override fun toString() = "ll_null"

    override fun asConstant() = NullConstant
}
