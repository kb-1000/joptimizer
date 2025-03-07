package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.constant.DoubleConstant

class LowLevelDoubleConstNode(var value: Double) : LowLevelNode(), LowLevelNoSideEffectsConstNode {
    override val size: Int
        get() = 2

    override fun toString() = "ll_double ${java.lang.Double.toHexString(value)}"

    override fun asConstant() = DoubleConstant(value)
}
