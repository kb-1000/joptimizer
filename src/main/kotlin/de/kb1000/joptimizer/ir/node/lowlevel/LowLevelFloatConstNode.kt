package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.constant.FloatConstant

class LowLevelFloatConstNode(var value: Float) : LowLevelNode(), LowLevelNoSideEffectsConstNode {
    override val size: Int
        get() = 1

    override fun toString() = "ll_float ${java.lang.Float.toHexString(value)}"

    override fun asConstant() = FloatConstant(value)
}
